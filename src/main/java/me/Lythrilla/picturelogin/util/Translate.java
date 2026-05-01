package me.Lythrilla.picturelogin.util;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;
import org.bukkit.ChatColor;

public class Translate {
    private static PictureLogin plugin;

    private Translate() {
        throw new IllegalStateException("Utility Class");
    }

    public static void init(PictureLogin plugin) {
        Translate.plugin = plugin;
    }

    public static String tl(String key) {
        return Translate.translate(key);
    }

    public static String tl(String key, Object ... args) {
        String message = Translate.translate(key);
        for (Object arg : args) {
            message = message.replaceFirst("%s", String.valueOf(arg));
        }
        return message;
    }

    private static String translate(String key) {
        if (plugin == null) {
            return "Plugin not initialized";
        }
        try {
            LanguageManager langManager = plugin.getConfigManager().getLanguageManager();
            if (langManager == null) {
                return key + " (LanguageManager\u672a\u521d\u59cb\u5316)";
            }
            String message = langManager.getMessage(key);
            if (message.equals(key)) {
                plugin.getLogger().warning("\u627e\u4e0d\u5230\u7ffb\u8bd1\u952e: " + key);
            }
            if (message.contains("%prefix%")) {
                String prefix = langManager.getMessage("prefix");
                message = message.replace("%prefix%", prefix);
            }
            message = message.replace("%new_line%", "\n");
            return Translate.color(message);
        }
        catch (Exception e) {
            plugin.getLogger().warning("\u7ffb\u8bd1\u65f6\u51fa\u9519: " + e.getMessage());
            return key + " (\u7ffb\u8bd1\u9519\u8bef)";
        }
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }
}
