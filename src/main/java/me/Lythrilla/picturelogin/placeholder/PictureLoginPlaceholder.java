package me.Lythrilla.picturelogin.placeholder;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.util.PictureUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PictureLoginPlaceholder
extends PlaceholderExpansion {
    private final PictureLogin plugin;
    private final PictureUtil pictureUtil;

    public PictureLoginPlaceholder(PictureLogin plugin) {
        this.plugin = plugin;
        this.pictureUtil = plugin.getPictureUtil();
    }

    public boolean canRegister() {
        return true;
    }

    public String getAuthor() {
        return "Lythrilla";
    }

    public String getIdentifier() {
        return "picturelogin";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        if (identifier.startsWith("avatar_")) {
            try {
                int lineNumber = Integer.parseInt(identifier.substring(7));
                String line = this.pictureUtil.getAvatarLine(player, lineNumber);
                return ChatColor.translateAlternateColorCodes((char)'&', (String)line);
            }
            catch (NumberFormatException e) {
                return "";
            }
        }
        if (identifier.startsWith("player_avatar_")) {
            try {
                String remainingIdentifier = identifier.substring(14);
                String[] parts = remainingIdentifier.split("_");
                if (parts.length != 2) {
                    return "";
                }
                String targetPlayerName = parts[0];
                int lineNumber = Integer.parseInt(parts[1]);
                String line = this.pictureUtil.getAvatarLineByName(targetPlayerName, lineNumber);
                if (line != null && !line.isEmpty()) {
                    return ChatColor.translateAlternateColorCodes((char)'&', (String)line);
                }
                return "";
            }
            catch (Exception e) {
                return "&c\u9519\u8bef&r";
            }
        }
        return null;
    }
}
