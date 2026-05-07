package org.pytenix.listeners;

import org.bukkit.event.Listener;
import org.pytenix.SessionTracePlugin;

public class DuplicateSessionListener implements Listener {

    private final SessionTracePlugin plugin;

    public DuplicateSessionListener(SessionTracePlugin plugin)
    {
        this.plugin = plugin;
    }


}
