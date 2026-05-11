package me.Lythrilla.picturelogin.util;

import java.util.regex.Pattern;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import me.Lythrilla.picturelogin.util.Hooks;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static BukkitAudiences adventure;
    private static final MiniMessage miniMessage;

    private MessageUtil() {
        throw new IllegalStateException("\u5de5\u5177\u7c7b\u4e0d\u5e94\u88ab\u5b9e\u4f8b\u5316");
    }

    public static void init(BukkitAudiences audiences) {
        adventure = audiences;
    }

    public static void shutdown() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public static String legacyToMiniMessage(String text) {
        if (text == null) {
            return "";
        }
        if (text.startsWith("<") && text.contains(">")) {
            return text;
        }
        text = HEX_PATTERN.matcher(text).replaceAll("<#$1>");
        text = ChatColor.translateAlternateColorCodes((char)'&', (String)text);
        text = text.replace("\u00a70", "<black>");
        text = text.replace("\u00a71", "<dark_blue>");
        text = text.replace("\u00a72", "<dark_green>");
        text = text.replace("\u00a73", "<dark_aqua>");
        text = text.replace("\u00a74", "<dark_red>");
        text = text.replace("\u00a75", "<dark_purple>");
        text = text.replace("\u00a76", "<gold>");
        text = text.replace("\u00a77", "<gray>");
        text = text.replace("\u00a78", "<dark_gray>");
        text = text.replace("\u00a79", "<blue>");
        text = text.replace("\u00a7a", "<green>");
        text = text.replace("\u00a7b", "<aqua>");
        text = text.replace("\u00a7c", "<red>");
        text = text.replace("\u00a7d", "<light_purple>");
        text = text.replace("\u00a7e", "<yellow>");
        text = text.replace("\u00a7f", "<white>");
        text = text.replace("\u00a7l", "<bold>");
        text = text.replace("\u00a7m", "<strikethrough>");
        text = text.replace("\u00a7n", "<underlined>");
        text = text.replace("\u00a7o", "<italic>");
        text = text.replace("\u00a7k", "<obfuscated>");
        text = text.replace("\u00a7r", "<reset>");
        return text;
    }

    public static Component formatMessage(Player player, String message) {
        boolean isMiniMessage;
        if (message == null) {
            return Component.empty();
        }
        boolean bl = isMiniMessage = message.startsWith("<") && message.contains(">");
        if (Hooks.PLACEHOLDER_API) {
            message = PlaceholderAPI.setPlaceholders((Player)player, (String)message);
        }
        message = message.replace("%pname%", player.getName());
        message = message.replace("%uuid%", player.getUniqueId().toString());
        message = message.replace("%online%", String.valueOf(player.getServer().getOnlinePlayers().size()));
        message = message.replace("%max%", String.valueOf(player.getServer().getMaxPlayers()));
        message = message.replace("%motd%", player.getServer().getMotd());
        message = message.replace("%displayname%", player.getDisplayName());
        if (!isMiniMessage) {
            message = MessageUtil.legacyToMiniMessage(message);
        }
        try {
            return miniMessage.deserialize(message);
        }
        catch (Exception e) {
            return Component.text(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
        }
    }

    public static void sendMessage(Player player, String message) {
        if (adventure == null || message == null || message.isEmpty()) {
            return;
        }
        Component component = MessageUtil.formatMessage(player, message);
        adventure.player(player).sendMessage(component);
    }

    static {
        miniMessage = MiniMessage.miniMessage();
    }
}
