package me.Lythrilla.picturelogin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.Lythrilla.picturelogin.PictureLogin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class UserManager {
    private final PictureLogin plugin;
    private File usersFile;
    private FileConfiguration usersConfig;
    private final Map<String, UserSettings> userSettingsCache = new HashMap<String, UserSettings>();

    public UserManager(PictureLogin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        if (this.usersFile == null) {
            this.usersFile = new File(this.plugin.getDataFolder(), "users.yml");
        }
        if (!this.usersFile.exists()) {
            this.plugin.saveResource("users.yml", false);
        }
        this.usersConfig = YamlConfiguration.loadConfiguration((File)this.usersFile);
        this.userSettingsCache.clear();
        ConfigurationSection playersSection = this.usersConfig.getConfigurationSection("players");
        if (playersSection != null) {
            for (String key : playersSection.getKeys(false)) {
                if (!playersSection.getBoolean(key + ".enabled", true)) continue;
                UserSettings settings = new UserSettings();
                settings.setMessages(playersSection.getStringList(key + ".messages"));
                settings.setFirstJoinMessages(playersSection.getStringList(key + ".first-join-messages"));
                settings.setLeaveMessages(playersSection.getStringList(key + ".leave-messages"));
                ConfigurationSection soundSection = playersSection.getConfigurationSection(key + ".sound");
                if (soundSection != null) {
                    settings.setSoundEnabled(soundSection.getBoolean("enabled", false));
                    settings.setSound(soundSection.getString("sound", "ENTITY_PLAYER_LEVELUP"));
                    settings.setVolume((float)soundSection.getDouble("volume", 1.0));
                    settings.setPitch((float)soundSection.getDouble("pitch", 1.0));
                }
                this.userSettingsCache.put(key.toLowerCase(), settings);
            }
        }
    }

    public void saveConfig() {
        if (this.usersFile == null || this.usersConfig == null) {
            return;
        }
        try {
            this.usersConfig.save(this.usersFile);
        }
        catch (Exception e) {
            this.plugin.getLogger().severe(this.plugin.getConfigManager().getLanguageManager().getMessage("log_user_save_error").replace("%error%", e.getMessage()));
        }
    }

    public UserSettings getUserSettings(Player player) {
        if (player == null) {
            return null;
        }
        String uuid = player.getUniqueId().toString().toLowerCase();
        if (this.userSettingsCache.containsKey(uuid)) {
            return this.userSettingsCache.get(uuid);
        }
        String name = player.getName().toLowerCase();
        return this.userSettingsCache.get(name);
    }

    public void setPlayerMessages(Player player, List<String> messages, String type) {
        String uuid = player.getUniqueId().toString();
        if (!this.usersConfig.contains("players." + uuid)) {
            this.usersConfig.set("players." + uuid + ".enabled", (Object)true);
        }
        this.usersConfig.set("players." + uuid + "." + type, messages);
        UserSettings settings = this.userSettingsCache.getOrDefault(uuid.toLowerCase(), new UserSettings());
        switch (type) {
            case "messages": {
                settings.setMessages(messages);
                break;
            }
            case "first-join-messages": {
                settings.setFirstJoinMessages(messages);
                break;
            }
            case "leave-messages": {
                settings.setLeaveMessages(messages);
            }
        }
        this.userSettingsCache.put(uuid.toLowerCase(), settings);
        this.saveConfig();
    }

    public void removePlayerSettings(Player player) {
        String uuid = player.getUniqueId().toString();
        this.usersConfig.set("players." + uuid, null);
        this.userSettingsCache.remove(uuid.toLowerCase());
        this.userSettingsCache.remove(player.getName().toLowerCase());
        this.saveConfig();
    }

    public int getUserCount() {
        return this.userSettingsCache.size();
    }

    public void setPlayerSound(Player player, boolean enabled, String sound, float volume, float pitch) {
        String uuid = player.getUniqueId().toString();
        if (!this.usersConfig.contains("players." + uuid)) {
            this.usersConfig.set("players." + uuid + ".enabled", (Object)true);
        }
        this.usersConfig.set("players." + uuid + ".sound.enabled", (Object)enabled);
        this.usersConfig.set("players." + uuid + ".sound.sound", (Object)sound);
        this.usersConfig.set("players." + uuid + ".sound.volume", (Object)Float.valueOf(volume));
        this.usersConfig.set("players." + uuid + ".sound.pitch", (Object)Float.valueOf(pitch));
        UserSettings settings = this.userSettingsCache.getOrDefault(uuid.toLowerCase(), new UserSettings());
        settings.setSoundEnabled(enabled);
        settings.setSound(sound);
        settings.setVolume(volume);
        settings.setPitch(pitch);
        this.userSettingsCache.put(uuid.toLowerCase(), settings);
        this.saveConfig();
    }

    public static class UserSettings {
        private List<String> messages = new ArrayList<String>();
        private List<String> firstJoinMessages = new ArrayList<String>();
        private List<String> leaveMessages = new ArrayList<String>();
        private boolean soundEnabled = false;
        private String sound = "ENTITY_PLAYER_LEVELUP";
        private float volume = 1.0f;
        private float pitch = 1.0f;

        public List<String> getMessages() {
            return this.messages;
        }

        public void setMessages(List<String> messages) {
            this.messages = messages != null ? messages : new ArrayList();
        }

        public List<String> getFirstJoinMessages() {
            return this.firstJoinMessages;
        }

        public void setFirstJoinMessages(List<String> firstJoinMessages) {
            this.firstJoinMessages = firstJoinMessages != null ? firstJoinMessages : new ArrayList();
        }

        public List<String> getLeaveMessages() {
            return this.leaveMessages;
        }

        public void setLeaveMessages(List<String> leaveMessages) {
            this.leaveMessages = leaveMessages != null ? leaveMessages : new ArrayList();
        }

        public boolean isSoundEnabled() {
            return this.soundEnabled;
        }

        public void setSoundEnabled(boolean soundEnabled) {
            this.soundEnabled = soundEnabled;
        }

        public String getSound() {
            return this.sound;
        }

        public void setSound(String sound) {
            this.sound = sound;
        }

        public float getVolume() {
            return this.volume;
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }

        public float getPitch() {
            return this.pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public boolean hasCustomMessages() {
            return !this.messages.isEmpty();
        }

        public boolean hasCustomFirstJoinMessages() {
            return !this.firstJoinMessages.isEmpty();
        }

        public boolean hasCustomLeaveMessages() {
            return !this.leaveMessages.isEmpty();
        }
    }
}
