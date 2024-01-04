package shx.kimono;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Kimono extends JavaPlugin implements Listener {
    private static Kimono plugin;
    private File scriptsFolder;
    private ScriptManager scriptManager;
    private StateStore stateStore;
    private DatabaseConnection databaseConnection;

    @Override
    public void onEnable() {
        plugin = this;
        defaultConfig();
        
        stateStore = new StateStore();
        databaseConnection = new DatabaseConnection();

        if ( !getDataFolder().exists() ) {
            getDataFolder().mkdir();
        }

        scriptsFolder = new File(getDataFolder(), "scripts");
        if ( !scriptsFolder.exists() ) {
            scriptsFolder.mkdir();
        }

        getServer().getScheduler().runTaskLater(this, () -> {
            scriptManager = new ScriptManager(this);
            scriptManager.load();
            getServer().getPluginManager().registerEvents(this, this);
        }, 1L);
    }

    @Override
    public void onDisable() {
        plugin = null;
        HandlerList.unregisterAll((JavaPlugin)this);
    }

    @EventHandler
    public void onPluginUnload(PluginDisableEvent e) {
        getLogger().info("event plugin unloaded");
    }

    @EventHandler
    public void onPluginLoad(PluginEnableEvent e) {
        getLogger().info("event plugin loaded");
    }

    private void defaultConfig() {
        FileConfiguration config = getConfig();

        config.addDefault("database.host", null);
        config.addDefault("database.port", null);
        config.addDefault("database.database", null);
        config.addDefault("database.username", null);
        config.addDefault("database.password", null);
        config.addDefault("database.maxPoolSize", 10);
        config.addDefault("database.connectionTimeout", 1000);
        config.addDefault("database.idleTimeout", 30000);
        config.addDefault("database.maxLifetime", 60000);

        config.options().copyDefaults(true);
        saveConfig();
    }

    public static Kimono getPlugin() {
        return plugin;
    }

    public File getScriptsFolder() {
        return scriptsFolder;
    }

    public StateStore getStateStore() {
        return stateStore;
    }

    public DatabaseConnection getDatabase() {
        return databaseConnection;
    }
}
