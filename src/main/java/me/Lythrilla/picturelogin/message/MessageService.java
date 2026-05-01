package me.Lythrilla.picturelogin.message;

import java.util.List;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.UserManager;
import org.bukkit.entity.Player;

public class MessageService {
    private final PictureLogin plugin;
    private final ConfigManager configManager;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final boolean enableUserMessages;
    private final boolean enablePermissionMessages;

    public MessageService(PictureLogin plugin, ConfigManager configManager, UserManager userManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.enableUserMessages = configManager.getBoolean("enable-user-messages", true);
        this.enablePermissionMessages = configManager.getBoolean("enable-permission-messages", true);
    }

    public List<String> getLoginMessages(Player player) {
        PermissionManager.PermissionGroup group;
        UserManager.UserSettings userSettings;
        if (this.enableUserMessages && (userSettings = this.userManager.getUserSettings(player)) != null && userSettings.hasCustomMessages()) {
            return userSettings.getMessages();
        }
        if (this.enablePermissionMessages && (group = this.permissionManager.getPlayerPermissionGroup(player)) != null && group.hasCustomMessages()) {
            return group.getMessages();
        }
        return this.configManager.getMessages();
    }

    public List<String> getFirstJoinMessages(Player player) {
        PermissionManager.PermissionGroup group;
        UserManager.UserSettings userSettings;
        if (this.enableUserMessages && (userSettings = this.userManager.getUserSettings(player)) != null && userSettings.hasCustomFirstJoinMessages()) {
            return userSettings.getFirstJoinMessages();
        }
        if (this.enablePermissionMessages && (group = this.permissionManager.getPlayerPermissionGroup(player)) != null && group.hasCustomFirstJoinMessages()) {
            return group.getFirstJoinMessages();
        }
        return this.configManager.getFirstJoinMessages();
    }

    public List<String> getLeaveMessages(Player player) {
        PermissionManager.PermissionGroup group;
        UserManager.UserSettings userSettings;
        if (this.enableUserMessages && (userSettings = this.userManager.getUserSettings(player)) != null && userSettings.hasCustomLeaveMessages()) {
            return userSettings.getLeaveMessages();
        }
        if (this.enablePermissionMessages && (group = this.permissionManager.getPlayerPermissionGroup(player)) != null && group.hasCustomLeaveMessages()) {
            return group.getLeaveMessages();
        }
        return this.configManager.getLeaveMessages();
    }

    public void reloadAll() {
        this.userManager.loadConfig();
        this.permissionManager.loadConfig();
    }
}
