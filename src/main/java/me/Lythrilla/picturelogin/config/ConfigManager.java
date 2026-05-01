package me.Lythrilla.picturelogin.config;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.util.imgmessage.ImageChar;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
    private final PictureLogin plugin;
    private LanguageManager languageManager;
    private YamlConfiguration config;
    private UserManager userManager;
    private PermissionManager permissionManager;

    public ConfigManager(PictureLogin plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration((File)configFile);
        this.languageManager = new LanguageManager(plugin, this);
    }

    public void reloadConfig() {
        try {
            File configFile = new File(this.plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                this.plugin.saveResource("config.yml", false);
            }
            this.config = YamlConfiguration.loadConfiguration((File)configFile);
            this.languageManager = this.languageManager != null ? new LanguageManager(this.plugin, this) : new LanguageManager(this.plugin, this);
            String language = this.getString("language", "en_US");
            this.languageManager.setLanguage(language);
            if (this.userManager != null) {
                this.userManager.loadConfig();
            }
            if (this.permissionManager != null) {
                this.permissionManager.loadConfig();
            }
            this.plugin.getLogger().info(this.languageManager.getMessage("log_plugin_reloaded"));
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("\u91cd\u65b0\u52a0\u8f7d\u914d\u7f6e\u65f6\u51fa\u9519: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private char getChar() {
        try {
            return ImageChar.valueOf(this.config.getString("character").toUpperCase()).getChar();
        }
        catch (IllegalArgumentException e) {
            return ImageChar.BLOCK.getChar();
        }
    }

    public ImageMessage getMessage(List<String> messages, BufferedImage image) {
        int imageDimensions = 8;
        int count = 0;
        ImageMessage imageMessage = new ImageMessage(image, imageDimensions, this.getChar());
        String[] msg = new String[imageDimensions];
        for (String message : messages) {
            if (count >= msg.length) break;
            msg[count++] = message;
        }
        while (count < imageDimensions) {
            msg[count++] = "";
        }
        if (this.config.getBoolean("center-text", false)) {
            return imageMessage.appendCenteredText(msg);
        }
        return imageMessage.appendText(msg);
    }

    public boolean getBoolean(String key) {
        return this.config.getBoolean(key);
    }

    public boolean getBoolean(String key, Boolean def) {
        return this.config.getBoolean(key, def.booleanValue());
    }

    public List<String> getStringList(String key) {
        return this.config.getStringList(key);
    }

    public String getString(String key) {
        return this.getString(key, null);
    }

    public String getString(String key, String def) {
        return this.config.getString(key, def);
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public String getMessage(String key) {
        if (this.languageManager == null) {
            this.languageManager = new LanguageManager(this.plugin, this);
        }
        return this.languageManager.getMessage(key);
    }

    public String getURL() {
        String url = this.config.getString("url");
        if (url == null) {
            this.plugin.getLogger().log(Level.SEVERE, this.languageManager.getMessage("log_url_error"));
            return "https://minepic.org/avatar/8/%uuid%";
        }
        return url;
    }

    public List<String> getMessages() {
        return this.getMessageList("messages", new ArrayList<String>());
    }

    public List<String> getFirstJoinMessages() {
        return this.getMessageList("first-join-messages", new ArrayList<String>());
    }

    public List<String> getLeaveMessages() {
        return this.getMessageList("leave-messages", new ArrayList<String>());
    }

    private List<String> getMessageList(String path, List<String> defaultValue) {
        if (!this.config.contains(path)) {
            return defaultValue;
        }
        List messages = this.config.getStringList(path);
        return messages.isEmpty() ? defaultValue : messages;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean isPlayLoginSound() {
        return this.config.getBoolean("play-login-sound", true);
    }

    public String getLoginSound() {
        return this.config.getString("login-sound.sound", "ENTITY_PLAYER_LEVELUP");
    }

    public float getLoginSoundVolume() {
        return (float)this.config.getDouble("login-sound.volume", 1.0);
    }

    public float getLoginSoundPitch() {
        return (float)this.config.getDouble("login-sound.pitch", 1.0);
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
}
