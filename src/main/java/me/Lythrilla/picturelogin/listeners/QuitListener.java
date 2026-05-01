package me.Lythrilla.picturelogin.listeners;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.util.PictureUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener
implements Listener {
    private PictureUtil pictureUtil;
    private ConfigManager config;

    public QuitListener(PictureLogin plugin) {
        this.config = plugin.getConfigManager();
        this.pictureUtil = plugin.getPictureUtil();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ImageMessage picture_message;
        if (!this.config.getBoolean("show-leave-message", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("picturelogin.show") && this.config.getBoolean("require-permission", true)) {
            return;
        }
        if (this.config.getBoolean("block-leave-message", true)) {
            event.setQuitMessage(null);
        }
        if ((picture_message = this.pictureUtil.createPictureMessage(player, this.config.getStringList("leave-messages"))) == null) {
            return;
        }
        this.pictureUtil.sendOutPictureMessage(picture_message);
    }
}
