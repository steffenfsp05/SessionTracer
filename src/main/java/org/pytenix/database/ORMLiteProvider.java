package org.pytenix.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.pytenix.database.models.ConnectionRecord;
import org.pytenix.database.models.WhitelistRecord;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ORMLiteProvider implements DatabaseProvider {

    private final File dbFile;
    private final ExecutorService executor;
    private ConnectionSource connectionSource;

    private Dao<ConnectionRecord, String> connectionDao;
    private Dao<WhitelistRecord, String> whitelistDao;

    public ORMLiteProvider(File dbFile) {
        this.dbFile = dbFile;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        String databaseUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.connectionSource = new JdbcConnectionSource(databaseUrl);

        this.connectionDao = DaoManager.createDao(connectionSource, ConnectionRecord.class);
        this.whitelistDao = DaoManager.createDao(connectionSource, WhitelistRecord.class);

        TableUtils.createTableIfNotExists(connectionSource, ConnectionRecord.class);
        TableUtils.createTableIfNotExists(connectionSource, WhitelistRecord.class);
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

        if (connectionSource != null) {
            connectionSource.close();
        }
    }

    @Override
    public CompletableFuture<Void> saveConnection(UUID uuid, String ip) {
        return CompletableFuture.runAsync(() -> {
            try {
                ConnectionRecord record = new ConnectionRecord(uuid.toString(), ip, new Date());
                connectionDao.createOrUpdate(record);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Set<UUID>> getAltsByIP(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ConnectionRecord> records = connectionDao.queryBuilder()
                        .where().eq("ip", ip)
                        .query();

                return records.stream()
                        .map(record -> UUID.fromString(record.getUuid()))
                        .collect(Collectors.toSet());

            } catch (SQLException e) {
                e.printStackTrace();
                return new HashSet<>();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> clearAll() {
        return CompletableFuture.runAsync(() -> {
            try {
                TableUtils.clearTable(connectionSource, ConnectionRecord.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Set<String>> getWhitelist() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return whitelistDao.queryForAll().stream()
                        .map(WhitelistRecord::getName)
                        .collect(Collectors.toSet());
            } catch (SQLException e) {
                e.printStackTrace();
                return new HashSet<>();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> addWhitelist(String name) {
        return CompletableFuture.runAsync(() -> {
            try {
                whitelistDao.createOrUpdate(new WhitelistRecord(name));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> isWhitelist(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return whitelistDao.idExists(name);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> removeWhitelist(String name) {
        return CompletableFuture.runAsync(() -> {
            try {
                whitelistDao.deleteById(name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }
}