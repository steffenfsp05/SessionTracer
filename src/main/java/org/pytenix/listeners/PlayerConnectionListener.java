package org.pytenix.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.pytenix.SessionTracePlugin;
import org.pytenix.config.ConfigService;
import org.pytenix.events.DuplicateSessionEvent;

import java.util.Arrays;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    final SessionTracePlugin plugin;
    final ConfigService configService;

    public PlayerConnectionListener(SessionTracePlugin plugin)
    {
        this.plugin = plugin;
        this.configService = plugin.getConfigService();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(AsyncPlayerPreLoginEvent event)
    {

        final UUID uuid = event.getUniqueId();
        final String userName = event.getName();

        final String ipAddress = event.getAddress().getHostAddress();


        final boolean isAllowed = plugin.getSessionTraceService().checkConnection(ipAddress, uuid, userName).join();

            if(!isAllowed)
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Component.text(configService.getConfiguration().getKickAltMessage()));
            else
                plugin.getSessionTraceService().traceConnection(uuid,ipAddress);

        plugin.getSessionTraceService().getAltAccounts(ipAddress).thenAccept(uuids -> {

            int knownAccounts = (uuids == null) ? 0 : uuids.size();

            if (uuids != null && uuids.contains(uuid))
                knownAccounts--;

            if (knownAccounts > 0) {
                DuplicateSessionEvent duplicateEvent = new DuplicateSessionEvent(
                        event.getName(),
                        uuid,
                        ipAddress,
                        knownAccounts,
                        uuids
                );
                Bukkit.getPluginManager().callEvent(duplicateEvent);
            }
        });


    }
}
