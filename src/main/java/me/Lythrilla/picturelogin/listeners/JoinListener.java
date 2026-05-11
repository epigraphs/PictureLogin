package me.Lythrilla.picturelogin.listeners;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.util.Hooks;
import me.Lythrilla.picturelogin.util.PictureUtil;
import me.Lythrilla.picturelogin.util.PictureWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinListener
implements Listener {
    private PictureLogin plugin;
    private PictureUtil pictureUtil;
    private ConfigManager config;
    private Player player;

    public JoinListener(PictureLogin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.pictureUtil = plugin.getPictureUtil();
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        this.player = event.getPlayer();
        if (this.config.getBoolean("block-join-message", false)) {
            event.setJoinMessage(null);
        }
        if (Hooks.AUTHME) {
            this.authMeLogin();
            return;
        }
        this.sendImage();
    }

    private void authMeLogin() {
        new BukkitRunnable(){

            public void run() {
                if (JoinListener.this.player == null || !JoinListener.this.player.isOnline()) {
                    this.cancel();
                }
                if (AuthMeApi.getInstance().isAuthenticated(JoinListener.this.player)) {
                    JoinListener.this.sendImage();
                    this.cancel();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void sendImage() {
        PictureWrapper wrapper = new PictureWrapper(this.plugin, this.player);
        if (this.config.getBoolean("async", true)) {
            wrapper.runTaskAsynchronously((Plugin)this.plugin);
        } else {
            wrapper.runTask((Plugin)this.plugin);
        }
    }
}
