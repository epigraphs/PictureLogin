package me.Lythrilla.picturelogin.util;

import java.util.logging.Logger;
import me.Lythrilla.picturelogin.config.ConfigManager;
import org.bukkit.plugin.PluginManager;

public class Hooks {
    private PluginManager plugins;
    private ConfigManager config;
    private Logger logger;
    public static boolean AUTHME;
    public static boolean PLACEHOLDER_API;
    public static boolean SKINSRESTORER;

    public Hooks(PluginManager plugins, ConfigManager config, Logger logger) {
        this.plugins = plugins;
        this.config = config;
        this.logger = logger;
        this.hookAuthMe();
        this.hookPlaceHolderAPI();
        this.hookSkinsRestorer();
    }

    private boolean hookPlugin(String plugin) {
        if (!this.plugins.isPluginEnabled(plugin)) {
            return false;
        }
        if (!this.config.getBoolean("hooks." + plugin, true)) {
            return false;
        }
        this.logger.info(() -> "Hooked into: " + plugin);
        return true;
    }

    private void hookAuthMe() {
        AUTHME = this.hookPlugin("AuthMe");
    }

    private void hookPlaceHolderAPI() {
        PLACEHOLDER_API = this.hookPlugin("PlaceholderAPI");
    }

    private void hookSkinsRestorer() {
        SKINSRESTORER = this.hookPlugin("SkinsRestorer");
    }
}
