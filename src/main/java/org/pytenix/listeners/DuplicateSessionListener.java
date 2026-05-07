package org.pytenix.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.pytenix.SessionTracePlugin;
import org.pytenix.events.DuplicateSessionEvent;

public class DuplicateSessionListener implements Listener {

    private final SessionTracePlugin plugin;

    public DuplicateSessionListener(SessionTracePlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onDuplicateSession(DuplicateSessionEvent event) {
        plugin.getNotificationService().notifyStaff(event);
    }

}
