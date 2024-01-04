package shx.kimono;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Kimono extends JavaPlugin implements Listener {
    private static Kimono plugin;

    public static Kimono getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        if ( !getDataFolder().exists() ) {
            getDataFolder().mkdir();
            saveResource("config.yml", false);
        }

        getServer().getScheduler().runTaskLater(this, () -> {
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
}
