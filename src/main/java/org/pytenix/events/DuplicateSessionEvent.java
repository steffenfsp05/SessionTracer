package org.pytenix.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;


@Getter
public class DuplicateSessionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String playerName;
    private final UUID playerUUID;
    private final String ipAddress;
    private final int knownAccountsOnIp;
    private final Set<UUID> accountUUIDs;

    public DuplicateSessionEvent(String playerName, UUID playerUUID, String ipAddress, int knownAccountsOnIp, Set<UUID> accountUUIDs) {
        super(true);
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.ipAddress = ipAddress;
        this.knownAccountsOnIp = knownAccountsOnIp;
        this.accountUUIDs = accountUUIDs;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
