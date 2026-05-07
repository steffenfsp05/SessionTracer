package org.pytenix.database;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DatabaseProvider {

    void init() throws Exception;

    void close() throws Exception;

    CompletableFuture<Void> saveConnection(UUID uuid, String ip);

    CompletableFuture<Set<UUID>> getAltsByIP(String ip);

    CompletableFuture<Void> clearAll();

    CompletableFuture<Set<String>> getWhitelist();

    CompletableFuture<Void> addWhitelist(String name);

    CompletableFuture<Boolean> isWhitelist(String name);

    CompletableFuture<Void> removeWhitelist(String name);

}
