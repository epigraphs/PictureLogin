package me.Lythrilla.picturelogin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageManager {
    private final PictureLogin plugin;
    private final ConfigManager configManager;
    private YamlConfiguration langConfig;
    private String currentLanguage;
    private List<String> availableLanguages = new ArrayList<String>();
    private boolean languagesLoaded = false;

    public LanguageManager(PictureLogin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.ensureLanguageFilesExist();
        this.loadLanguages();
        String language = configManager.getString("language", "en_US");
        this.setLanguage(language);
        plugin.getLogger().info(this.getLogMessage("log_language_manager_init", "%language%", this.currentLanguage));
    }

    public void reloadLanguage() {
        try {
            this.availableLanguages = this.getAvailableLanguages();
            String language = this.configManager.getString("language", "en_US");
            this.setLanguage(language);
            this.plugin.getLogger().info(this.getLogMessage("log_language_changed", "%language%", this.currentLanguage));
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("\u91cd\u65b0\u52a0\u8f7d\u8bed\u8a00\u6587\u4ef6\u65f6\u51fa\u9519: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureLanguageFilesExist() {
        File langDir = new File(this.plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
            this.plugin.getLogger().info(this.getLogMessage("log_creating_lang_dir", "%path%", langDir.getPath()));
        }
        File enFile = new File(langDir, "en_US.yml");
        File zhFile = new File(langDir, "zh_CN.yml");
        if (!enFile.exists()) {
            this.plugin.saveResource("lang/en_US.yml", false);
            this.plugin.getLogger().info(this.getLogMessage("log_creating_en_file", "%path%", enFile.getPath()));
        }
        if (!zhFile.exists()) {
            this.plugin.saveResource("lang/zh_CN.yml", false);
            this.plugin.getLogger().info(this.getLogMessage("log_creating_zh_file", "%path%", zhFile.getPath()));
        }
    }

    private YamlConfiguration getLanguageConfig(String lang) {
        if (this.langConfig != null && this.currentLanguage != null && this.currentLanguage.equals(lang)) {
            return this.langConfig;
        }
        File langFile = new File(this.plugin.getDataFolder(), "lang" + File.separator + lang + ".yml");
        if (!langFile.exists()) {
            this.plugin.getLogger().log(Level.WARNING, this.getLogMessage("log_language_not_found", new String[]{"%language%", "%default%"}, new String[]{lang, "en_US"}));
            this.ensureLanguageFilesExist();
            if (!lang.equals("en_US")) {
                return this.getLanguageConfig("en_US");
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)langFile);
        this.plugin.getLogger().fine("\u5df2\u52a0\u8f7d\u8bed\u8a00\u6587\u4ef6: " + lang);
        return config;
    }

    public void setLanguage(String language) {
        if (this.currentLanguage != null && this.currentLanguage.equals(language)) {
            this.plugin.getLogger().info("\u8bed\u8a00\u672a\u6539\u53d8\uff0c\u4ecd\u4e3a: " + language);
            return;
        }
        List<String> availableLanguages = this.getAvailableLanguages();
        if (!availableLanguages.contains(language)) {
            this.plugin.getLogger().warning(this.getLogMessage("log_language_not_found", new String[]{"%language%", "%default%"}, new String[]{language, "en_US"}));
            language = "en_US";
        }
        try {
            File configFile = new File(this.plugin.getDataFolder(), "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration((File)configFile);
            config.set("language", (Object)language);
            config.save(configFile);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("\u65e0\u6cd5\u4fdd\u5b58\u8bed\u8a00\u8bbe\u7f6e: " + e.getMessage());
        }
        this.currentLanguage = language;
        this.langConfig = this.getLanguageConfig(language);
        this.loadLanguages();
        this.plugin.getLogger().info(this.getLogMessage("log_language_changed", "%language%", language));
    }

    public String getCurrentLanguage() {
        return this.currentLanguage;
    }

    public String getMessage(String key) {
        String prefix;
        String message;
        if (this.langConfig == null) {
            this.langConfig = this.getLanguageConfig("en_US");
            if (this.langConfig == null) {
                return key;
            }
        }
        if ((message = this.langConfig.getString(key)) == null && !this.currentLanguage.equals("en_US")) {
            YamlConfiguration enConfig = this.getLanguageConfig("en_US");
            message = enConfig.getString(key);
        }
        if (message == null) {
            this.plugin.getLogger().warning("\u627e\u4e0d\u5230\u8bed\u8a00\u952e: " + key + " \u5728\u8bed\u8a00 " + this.currentLanguage);
            return key;
        }
        if (message.contains("%prefix%") && (prefix = this.langConfig.getString("prefix")) != null) {
            message = message.replace("%prefix%", prefix);
        }
        message = message.replace("%new_line%", "\n");
        return message;
    }

    public List<String> getAvailableLanguages() {
        File[] files;
        ArrayList<String> languages = new ArrayList<String>();
        File langDir = new File(this.plugin.getDataFolder(), "lang");
        if (langDir.exists() && langDir.isDirectory() && (files = langDir.listFiles((dir, name) -> name.endsWith(".yml"))) != null) {
            for (File file : files) {
                String langCode = file.getName().replace(".yml", "");
                languages.add(langCode);
            }
        }
        if (languages.isEmpty()) {
            languages.add("en_US");
        }
        return languages;
    }

    public boolean isLanguageAvailable(String language) {
        return this.availableLanguages.contains(language);
    }

    public void loadLanguages() {
        if (!this.languagesLoaded) {
            this.availableLanguages = this.getAvailableLanguages();
            this.languagesLoaded = true;
            this.plugin.getLogger().info(this.getLogMessage("log_languages_loaded", "%languages%", this.availableLanguages.toString()));
        }
    }

    private String getLogMessage(String key, String placeholder, String value) {
        if (this.langConfig == null) {
            return key + ": " + value;
        }
        String message = this.langConfig.getString(key);
        if (message == null) {
            return key + ": " + value;
        }
        return message.replace(placeholder, value);
    }

    private String getLogMessage(String key, String[] placeholders, String[] values) {
        if (placeholders.length != values.length) {
            return key;
        }
        if (this.langConfig == null) {
            StringBuilder result = new StringBuilder(key);
            result.append(": ");
            for (int i = 0; i < placeholders.length; ++i) {
                result.append(placeholders[i]).append("=").append(values[i]);
                if (i >= placeholders.length - 1) continue;
                result.append(", ");
            }
            return result.toString();
        }
        String message = this.langConfig.getString(key);
        if (message == null) {
            StringBuilder result = new StringBuilder(key);
            result.append(": ");
            for (int i = 0; i < placeholders.length; ++i) {
                result.append(placeholders[i]).append("=").append(values[i]);
                if (i >= placeholders.length - 1) continue;
                result.append(", ");
            }
            return result.toString();
        }
        for (int i = 0; i < placeholders.length; ++i) {
            message = message.replace(placeholders[i], values[i]);
        }
        return message;
    }
}
