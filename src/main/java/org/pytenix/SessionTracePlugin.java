package org.pytenix;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.pytenix.cache.CacheProvider;
import org.pytenix.cache.CaffeineCacheProvider;
import org.pytenix.command.SessionTraceCommand;
import org.pytenix.config.ConfigService;
import org.pytenix.database.DatabaseProvider;
import org.pytenix.database.ORMLiteProvider;
import org.pytenix.database.SQLiteProvider;
import org.pytenix.listeners.DuplicateSessionListener;
import org.pytenix.listeners.PlayerConnectionListener;
import org.pytenix.services.NotificationService;
import org.pytenix.services.SessionTraceService;
import org.pytenix.services.WhitelistService;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class SessionTracePlugin extends JavaPlugin {

    private ConfigService configService;
    private DatabaseProvider playerDatabase;
    private CacheProvider<String, Set<UUID>> playerCache;
    private CacheProvider<String, WhitelistService.Status> whitelistCache;

    private SessionTraceService sessionTraceService;
    private WhitelistService whitelistService;
    private NotificationService notificationService;


    //THIS SETUP DOESNT WORK IF PROXIES ARE ENABLED (e.g. TCPShield)
    @Override
    public void onEnable() {
        try {

            if (!getDataFolder().exists())
                getDataFolder().mkdirs();

            this.configService = new ConfigService(this, getDataFolder());

            this.playerDatabase = new ORMLiteProvider(new File(getDataFolder(), "orm_database.db"));
            this.playerCache = new CaffeineCacheProvider<>(10, TimeUnit.SECONDS);
            this.whitelistCache = new CaffeineCacheProvider<>(10, TimeUnit.SECONDS);

            this.playerDatabase.init();

            this.sessionTraceService = new SessionTraceService(this);
            this.whitelistService = new WhitelistService(this);
            this.notificationService = new NotificationService(this);

            getLogger().info("SessionTracer successfully started!");

            registerListeners();
            registerCommands();

            initOnlinePlayer();

        } catch (Exception e) {
            getLogger().severe("Critical error while starting the plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }


    private void registerCommands() {
        this.getCommand("trace").setExecutor(new SessionTraceCommand(this));
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerConnectionListener(this), this);
        pluginManager.registerEvents(new DuplicateSessionListener(this), this);
    }


    private void initOnlinePlayer() {
        Bukkit.getOnlinePlayers().forEach(player ->
        {
            final InetSocketAddress address = player.getAddress();

            if (address != null)
                sessionTraceService.traceConnection(player.getUniqueId(), address.getHostString());
        });
    }

    @Override
    public void onDisable() {

        if (this.playerCache != null)
            playerCache.clearCache();

        if (this.whitelistCache != null)
            whitelistCache.clearCache();

        if (this.playerDatabase != null)
            try {
                this.playerDatabase.close();
                getLogger().info("Database Connection successfully closed!");
            } catch (Exception e) {
                getLogger().severe("An error occured while closing the database connection: " + e.getMessage());
            }

    }

}
