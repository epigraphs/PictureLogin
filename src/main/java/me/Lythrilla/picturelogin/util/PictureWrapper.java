package me.Lythrilla.picturelogin.util;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.util.PictureUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PictureWrapper
extends BukkitRunnable {
    private PictureUtil pictureUtil;
    private ConfigManager config;
    private Player player;
    private PictureLogin plugin;

    public PictureWrapper(PictureLogin plugin, Player player) {
        this.pictureUtil = plugin.getPictureUtil();
        this.config = plugin.getConfigManager();
        this.player = player;
        this.plugin = plugin;
    }

    public void run() {
        this.sendImage();
        this.playSound();
    }

    private boolean checkPermission() {
        if (!this.config.getBoolean("require-permission", true)) {
            return true;
        }
        return this.player.hasPermission("picturelogin.show");
    }

    private ImageMessage getMessage() {
        String msgType = this.config.getBoolean("show-first-join", true) && !this.player.hasPlayedBefore() ? "first-join-messages" : "messages";
        return this.pictureUtil.createPictureMessage(this.player, this.config.getStringList(msgType));
    }

    private void sendImage() {
        if (!this.checkPermission()) {
            return;
        }
        ImageMessage pictureMessage = this.getMessage();
        if (pictureMessage == null) {
            return;
        }
        if (this.config.getBoolean("player-only", true)) {
            if (this.config.getBoolean("clear-chat", false)) {
                this.pictureUtil.clearChat(this.player);
            }
            pictureMessage.sendToPlayer(this.player);
            return;
        }
        this.pictureUtil.sendOutPictureMessage(pictureMessage);
    }

    private void playSound() {
        if (!this.config.isPlayLoginSound()) {
            return;
        }
        try {
            PermissionManager.PermissionGroup permGroup;
            UserManager.UserSettings userSettings;
            UserManager userManager = this.plugin.getConfigManager().getUserManager();
            if (userManager != null && this.config.getBoolean("enable-user-messages", true) && (userSettings = userManager.getUserSettings(this.player)) != null && userSettings.isSoundEnabled()) {
                this.playLoginSound(userSettings.getSound(), userSettings.getVolume(), userSettings.getPitch());
                return;
            }
            PermissionManager permManager = this.plugin.getConfigManager().getPermissionManager();
            if (permManager != null && this.config.getBoolean("enable-permission-messages", true) && (permGroup = permManager.getPlayerPermissionGroup(this.player)) != null && permGroup.isSoundEnabled()) {
                this.playLoginSound(permGroup.getSound(), permGroup.getVolume(), permGroup.getPitch());
                return;
            }
            this.playLoginSound(this.config.getLoginSound(), this.config.getLoginSoundVolume(), this.config.getLoginSoundPitch());
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("\u64ad\u653e\u767b\u5f55\u97f3\u6548\u65f6\u51fa\u9519: " + e.getMessage());
        }
    }

    private void playLoginSound(String soundName, float volume, float pitch) {
        try {
            // Use the String overload so we work across Bukkit versions that
            // moved Sound from an enum to a Registry-backed interface (1.21+).
            // Accept either enum-style (ENTITY_PLAYER_LEVELUP) or
            // namespaced-key (minecraft:entity.player.levelup) input.
            String key = soundName;
            if (!key.contains(".") && !key.contains(":")) {
                key = key.toLowerCase().replace('_', '.');
            }
            this.player.playSound(this.player.getLocation(), key, volume, pitch);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Invalid sound name: " + soundName);
        }
    }
}
