package me.Lythrilla.picturelogin;

import me.Lythrilla.picturelogin.commands.BaseCommand;
import me.Lythrilla.picturelogin.commands.PictureLoginTabCompleter;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.UserManager;
import org.bstats.bukkit.Metrics;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import me.Lythrilla.picturelogin.listeners.JoinListener;
import me.Lythrilla.picturelogin.listeners.QuitListener;
import me.Lythrilla.picturelogin.message.MessageService;
import me.Lythrilla.picturelogin.placeholder.PictureLoginPlaceholder;
import me.Lythrilla.picturelogin.util.Hooks;
import me.Lythrilla.picturelogin.util.MessageUtil;
import me.Lythrilla.picturelogin.util.PictureUtil;
import me.Lythrilla.picturelogin.util.Translate;
import me.Lythrilla.picturelogin.util.Updater;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class PictureLogin
extends JavaPlugin {
    private ConfigManager configManager;
    private PictureUtil pictureUtil;
    private BukkitAudiences adventure;
    private UserManager userManager;
    private PermissionManager permissionManager;
    private MessageService messageService;
    private BukkitTask announcerTask;
    private long startTime;
    public static SkinsRestorer skinsRestorerAPI;
    private static PictureLogin instance;

    public static PictureLogin getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        this.startTime = System.currentTimeMillis();
        this.displayStartupArt();
        this.configManager = new ConfigManager(this);
        this.userManager = new UserManager(this);
        this.permissionManager = new PermissionManager(this);
        this.configManager.setUserManager(this.userManager);
        this.configManager.setPermissionManager(this.permissionManager);
        this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_players_loaded").replace("%count%", String.valueOf(this.userManager.getUserCount())));
        this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_permissions_loaded").replace("%count%", String.valueOf(this.permissionManager.getGroupCount())));
        this.adventure = BukkitAudiences.create((Plugin)this);
        Translate.init(this);
        new Hooks(this.getServer().getPluginManager(), this.configManager, this.getLogger());
        this.messageService = new MessageService(this, this.configManager, this.userManager, this.permissionManager);
        if (Hooks.SKINSRESTORER) {
            skinsRestorerAPI = SkinsRestorerProvider.get();
        }
        MessageUtil.init(this.adventure);
        this.pictureUtil = new PictureUtil(this);
        this.getServer().getPluginManager().registerEvents((Listener)new JoinListener(this), (Plugin)this);
        if (this.configManager.getBoolean("show-leave-message", false)) {
            this.getServer().getPluginManager().registerEvents((Listener)new QuitListener(this), (Plugin)this);
        }
        this.getCommand("picturelogin").setExecutor((CommandExecutor)new BaseCommand(this));
        this.getCommand("picturelogin").setTabCompleter((TabCompleter)new PictureLoginTabCompleter(this));
        if (Hooks.PLACEHOLDER_API && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PictureLoginPlaceholder(this).register();
            this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_papi_registered"));
        }
        if (this.configManager.getBoolean("update-check", true)) {
            this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_update_check_enabled").replace("%version%", this.getDescription().getVersion()));
            new Updater(this.getLogger(), this.getDescription().getVersion());
        } else {
            this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_update_check_disabled").replace("%version%", this.getDescription().getVersion()));
        }
        if (this.configManager.getBoolean("metrics", true)) {
            new Metrics(this, 15033);
        }
        long loadTime = System.currentTimeMillis() - this.startTime;
        this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_plugin_enabled").replace("%version%", this.getDescription().getVersion()) + " (" + loadTime + "ms)");
    }

    private void displayStartupArt() {
        String[] asciiArt;
        for (String line : asciiArt = new String[]{"", " _____  _      _                     _                _       ", "|  __ \\(_)    | |                   | |              (_)      ", "| |__) |  ___| |_ _   _ _ __ ___   | |     ___   __ _ _ _ __ ", "|  ___/ |/ __| __| | | | '__/ _ \\  | |    / _ \\ / _` | | '_ \\", "| |   | | (__| |_| |_| | | |  __/  | |___| (_) | (_| | | | | |", "|_|   |_|\\___|\\__|\\__,_|_|  \\___|  |______\\___/ \\___|_|_| |_|", "", "PictureLogin R2", "By Lythrilla | Original by NathanG", "GitHub: https://github.com/Lythrilla/PictureLogin-master", ""}) {
            this.getLogger().info(line);
        }
    }

    public void onDisable() {
        if (this.adventure != null) {
            MessageUtil.shutdown();
            this.adventure.close();
            this.adventure = null;
        }
        if (this.configManager != null && this.configManager.getLanguageManager() != null) {
            this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_plugin_disabled"));
        } else {
            this.getLogger().info("PictureLogin disabled!");
        }
    }

    public void reloadPlugin() {
        this.configManager.reloadConfig();
        if (this.messageService != null) {
            this.messageService = new MessageService(this, this.configManager, this.userManager, this.permissionManager);
        }
        if (this.pictureUtil != null) {
            this.pictureUtil = new PictureUtil(this);
        }
        if (this.pictureUtil != null) {
            this.pictureUtil.clearAllAvatarCaches();
        }
        this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_plugin_reloaded"));
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public PictureUtil getPictureUtil() {
        return this.pictureUtil;
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException(this.configManager.getLanguageManager().getMessage("log_adventure_error"));
        }
        return this.adventure;
    }

    public MessageService getMessageService() {
        return this.messageService;
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
}
