package org.pytenix.services;

import org.pytenix.SessionTracePlugin;

import java.util.concurrent.CompletableFuture;

public class WhitelistService {

    private final SessionTracePlugin plugin;

    public WhitelistService(SessionTracePlugin plugin)
    {
        this.plugin = plugin;
    }

    public CompletableFuture<Status> getWhitelistStatus(String userName)
    {
        CompletableFuture<Boolean> future;
        Status cachedValue = plugin.getWhitelistCache().get(userName);

        if (cachedValue != null) {
            future = CompletableFuture.completedFuture(cachedValue == Status.WHITELISTED);
        } else {
            future = plugin.getPlayerDatabase().isWhitelist(userName);
        }

        return future.thenApply(aBoolean ->
        {
            Status status = aBoolean ? Status.WHITELISTED : Status.NOT_WHITELISTED;
            plugin.getWhitelistCache().put(userName, status);
            return status;

        });

    }

    public CompletableFuture<Result> addToWhitelist(String userName) {
        return modifyWhitelist(userName, true);
    }

    public CompletableFuture<Result> removeFromWhitelist(String userName) {
        return modifyWhitelist(userName, false);
    }

    public CompletableFuture<Result> modifyWhitelist(String userName, boolean add) {
        CompletableFuture<Status> future = getWhitelistStatus(userName);


        return future.thenCompose(isCurrentlyWhitelisted -> {

            if (isCurrentlyWhitelisted.equals(Status.WHITELISTED) == add) {
                return CompletableFuture.completedFuture(add ? Result.DUPLICATE : Result.NOT_EXISTS);
            }

            CompletableFuture<Void> dbAction = add
                    ? plugin.getPlayerDatabase().addWhitelist(userName)
                    : plugin.getPlayerDatabase().removeWhitelist(userName);

            return dbAction.thenApply(unused -> {
                plugin.getWhitelistCache().put(userName, add ? Status.WHITELISTED : Status.NOT_WHITELISTED);

                return Result.SUCCESS;
            });

        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return Result.ERROR;
        });
    }
    public enum Status {
        WHITELISTED,NOT_WHITELISTED
    }

    public enum Result {
        DUPLICATE, NOT_EXISTS, SUCCESS, ERROR
    }
}
