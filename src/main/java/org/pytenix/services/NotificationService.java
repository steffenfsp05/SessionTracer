package org.pytenix.services;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pytenix.SessionTracePlugin;
import org.pytenix.config.ConfigService;


public class NotificationService {

    private final SessionTracePlugin plugin;
    private final ConfigService configService;

    public NotificationService(SessionTracePlugin plugin)
    {
        this.plugin = plugin;
        this.configService = plugin.getConfigService();
    }

    public void notifyStaff(String originalName, String ipAddress) {

        String permission = configService.getConfiguration().getStaffPermission();
        String rawMessage = configService.getConfiguration().getStaffNotifyMessage();

        String formattedMessage = String.format(rawMessage, originalName, ipAddress);

        Component alertMessage = Component.text(formattedMessage).color(NamedTextColor.RED);

        Bukkit.getScheduler().runTask(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                    player.sendMessage(alertMessage);
                }
            }

            Bukkit.getConsoleSender().sendMessage(alertMessage);
        });
    }

}
