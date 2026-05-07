package org.pytenix.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pytenix.SessionTracePlugin;
import org.pytenix.config.ConfigService;
import org.pytenix.events.DuplicateSessionEvent;
import org.pytenix.listeners.DuplicateSessionListener;

import javax.lang.model.element.ElementVisitor;
import java.util.function.Function;
import java.util.stream.Collectors;


public class NotificationService {

    private final SessionTracePlugin plugin;
    private final ConfigService configService;

    public NotificationService(SessionTracePlugin plugin)
    {
        this.plugin = plugin;
        this.configService = plugin.getConfigService();
    }

    public void notifyStaff(DuplicateSessionEvent event) {

        String permission = configService.getConfiguration().getStaffPermission();
        String rawMessage = configService.getConfiguration().getStaffNotifyMessage();

        String formattedMessage = formatMessage(rawMessage, event);

        Component alertMessage = LegacyComponentSerializer.legacySection().deserialize(formattedMessage);

        Bukkit.getScheduler().runTask(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                    player.sendMessage(alertMessage);
                }
            }

            Bukkit.getConsoleSender().sendMessage(alertMessage);
        });
    }

    public String formatMessage(String rawMessage, DuplicateSessionEvent event)
    {
        for (Placeholder value : Placeholder.values()) {
            rawMessage = rawMessage.replace("%"+value.placeholderId+"%", value.getFunc().apply(event));
        }
        return configService.getConfiguration().getPrefix() +" "+ rawMessage;
    }


    @AllArgsConstructor @Getter
    enum Placeholder
    {
        originalName("name", DuplicateSessionEvent::getPlayerName),
        ipaddress("ipaddress", DuplicateSessionEvent::getIpAddress),
        duplicateNames("duplicatenames", event ->
                event.getAccountUUIDs().stream().filter(uuid -> !uuid.equals(event.getPlayerUUID())).map(uuid ->
                        Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : (Bukkit.getOfflinePlayer(uuid) != null ? Bukkit.getOfflinePlayer(uuid).getName() : "Not available")
                ).collect(Collectors.joining(","))),
        knownAccounts("knownaccounts", event -> String.valueOf(event.getKnownAccountsOnIp())),
        playerUuid("playeruuid", event -> event.getPlayerUUID().toString());

        String placeholderId;
        Function<DuplicateSessionEvent,String> func;


    }

}
