package org.pytenix.database;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SQLiteProvider implements DatabaseProvider {
    private final File dbFile;
    private final ExecutorService executor;
    private Connection connection;

    //UNSAFE; RECOMMENDATION: ORMLiteProvider

    public SQLiteProvider(File dbFile) {
        this.dbFile = dbFile;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS connections (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "ip VARCHAR(45) NOT NULL," +
                    "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (uuid)" +
                    ");");
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS whitelist (" +
                    "name VARCHAR(36) NOT NULL," +
                    "PRIMARY KEY (name)" +
                    ");");
        }
    }

    @Override
    public void close() throws Exception {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public CompletableFuture<Void> saveConnection(UUID uuid, String ip) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO connections (uuid, ip, last_seen) VALUES (?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, ip);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Set<UUID>> getAltsByIP(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            Set<UUID> alts = new HashSet<>();
            String sql = "SELECT uuid FROM connections WHERE ip = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, ip);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    alts.add(UUID.fromString(rs.getString("uuid")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return alts;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> clearAll() {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM connections";
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Set<String>> getWhitelist() {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> userNames = new HashSet<>();
            String sql = "SELECT name FROM whitelist";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    userNames.add(rs.getString("name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return userNames;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> addWhitelist(String name) {

        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO whitelist (name) VALUES (?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> isWhitelist(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM whitelist WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();

                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> removeWhitelist(String name) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM whitelist WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

}
