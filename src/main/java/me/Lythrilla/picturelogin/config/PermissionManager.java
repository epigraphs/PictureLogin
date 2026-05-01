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

public class PermissionManager {
    private final PictureLogin plugin;
    private File permsFile;
    private FileConfiguration permsConfig;
    private final Map<String, PermissionGroup> permGroupCache = new HashMap<String, PermissionGroup>();

    public PermissionManager(PictureLogin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        if (this.permsFile == null) {
            this.permsFile = new File(this.plugin.getDataFolder(), "perms.yml");
        }
        if (!this.permsFile.exists()) {
            this.plugin.saveResource("perms.yml", false);
        }
        this.permsConfig = YamlConfiguration.loadConfiguration((File)this.permsFile);
        this.permGroupCache.clear();
        ConfigurationSection groupsSection = this.permsConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String key : groupsSection.getKeys(false)) {
                if (!groupsSection.getBoolean(key + ".enabled", true)) continue;
                String permission = groupsSection.getString(key + ".permission");
                int priority = groupsSection.getInt(key + ".priority", 0);
                if (permission == null || permission.isEmpty()) continue;
                PermissionGroup group = new PermissionGroup(key, permission, priority);
                group.setMessages(groupsSection.getStringList(key + ".messages"));
                group.setFirstJoinMessages(groupsSection.getStringList(key + ".first-join-messages"));
                group.setLeaveMessages(groupsSection.getStringList(key + ".leave-messages"));
                ConfigurationSection soundSection = groupsSection.getConfigurationSection(key + ".sound");
                if (soundSection != null) {
                    group.setSoundEnabled(soundSection.getBoolean("enabled", false));
                    group.setSound(soundSection.getString("sound", "ENTITY_PLAYER_LEVELUP"));
                    group.setVolume((float)soundSection.getDouble("volume", 1.0));
                    group.setPitch((float)soundSection.getDouble("pitch", 1.0));
                }
                this.permGroupCache.put(permission, group);
            }
        }
    }

    public void saveConfig() {
        if (this.permsFile == null || this.permsConfig == null) {
            return;
        }
        try {
            this.permsConfig.save(this.permsFile);
        }
        catch (Exception e) {
            this.plugin.getLogger().severe(this.plugin.getConfigManager().getLanguageManager().getMessage("log_permission_save_error").replace("%error%", e.getMessage()));
        }
    }

    public int getGroupCount() {
        return this.permGroupCache.size();
    }

    public PermissionGroup getPlayerPermissionGroup(Player player) {
        if (player == null) {
            return null;
        }
        PermissionGroup highestGroup = null;
        for (PermissionGroup group : this.permGroupCache.values()) {
            if (!player.hasPermission(group.getPermission()) || highestGroup != null && group.getPriority() <= highestGroup.getPriority()) continue;
            highestGroup = group;
        }
        return highestGroup;
    }

    public List<PermissionGroup> getPermissionGroups() {
        return new ArrayList<PermissionGroup>(this.permGroupCache.values());
    }

    public static class PermissionGroup {
        private final String name;
        private final String permission;
        private final int priority;
        private List<String> messages = new ArrayList<String>();
        private List<String> firstJoinMessages = new ArrayList<String>();
        private List<String> leaveMessages = new ArrayList<String>();
        private boolean soundEnabled = false;
        private String sound = "ENTITY_PLAYER_LEVELUP";
        private float volume = 1.0f;
        private float pitch = 1.0f;

        public PermissionGroup(String name, String permission, int priority) {
            this.name = name;
            this.permission = permission;
            this.priority = priority;
        }

        public String getName() {
            return this.name;
        }

        public String getPermission() {
            return this.permission;
        }

        public int getPriority() {
            return this.priority;
        }

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
