package org.pytenix.services;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pytenix.SessionTracePlugin;
import org.pytenix.config.ConfigService;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SessionTraceService {


    private final SessionTracePlugin plugin;
    private final ConfigService configService;

    public SessionTraceService(SessionTracePlugin plugin)
    {
        this.plugin = plugin;
        this.configService = plugin.getConfigService();
    }


    public CompletableFuture<Boolean> checkConnection(String ipAddress, UUID uuid, String userName)
    {
        CompletableFuture<Set<UUID>> altsFuture = getAltAccounts(ipAddress);
        CompletableFuture<WhitelistService.Status> whitelistFuture = plugin.getWhitelistService().getWhitelistStatus(userName.toLowerCase());

        return altsFuture.thenCombine(whitelistFuture, (uuids, whitelistStatus) -> {

            if (whitelistStatus.equals(WhitelistService.Status.WHITELISTED))
                return true;


            if (uuids == null || uuids.isEmpty() || uuids.contains(uuid))
                return true;


            return uuids.size() < configService.getConfiguration().getMaxAccountsPerIp();
        });
    }


    public void traceConnection(UUID uuid, String ip)
    {
        Set<UUID> uuids = plugin.getPlayerCache().get(ip, s -> ConcurrentHashMap.newKeySet());

        uuids.add(uuid);

        plugin.getPlayerDatabase().saveConnection(uuid,ip);


    }
    public void resetAndReinitialize(Runnable callback) {

        plugin.getPlayerCache().clearCache();

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        plugin.getPlayerDatabase().clearAll().thenRun(() -> {

            List<CompletableFuture<Void>> playerTasks = new ArrayList<>();

            for (Player player : onlinePlayers) {
                final InetSocketAddress playerAddress = player.getAddress();
                if (playerAddress != null) {
                    final String address = playerAddress.getAddress().getHostAddress();

                    CompletableFuture<Void> task = plugin.getSessionTraceService()
                            .checkConnection(address, player.getUniqueId(), player.getName())
                            .thenAccept(isAllowed -> {

                                if (!isAllowed) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        player.kick(Component.text(configService.getConfiguration().getKickAltMessage()));
                                    });
                                } else {
                                    traceConnection(player.getUniqueId(), address);
                                }

                            });

                    playerTasks.add(task);
                }
            }

            CompletableFuture.allOf(playerTasks.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        if (callback != null) {
                            callback.run();
                        }
                    });
        });
    }
    public CompletableFuture<Set<UUID>> getAltAccounts(String ip) {

        Set<UUID> cachedUuids = plugin.getPlayerCache().get(ip);
        
        if (cachedUuids != null && !cachedUuids.isEmpty()) {
            System.out.println("cachedUuids: "+cachedUuids.stream().map(uuid -> uuid+":").collect(Collectors.joining()));
            return CompletableFuture.completedFuture(cachedUuids);
        }


        return plugin.getPlayerDatabase().getAltsByIP(ip)
                .thenApply(list -> {
                    Set<UUID> concurrentSet = plugin.getPlayerCache().get(ip, s -> ConcurrentHashMap.newKeySet());

                    if (list != null) {
                        concurrentSet.addAll(list);
                    }


                    return concurrentSet;
                });
    }



}
