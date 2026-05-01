package me.Lythrilla.picturelogin.util;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.FallbackPicture;
import me.Lythrilla.picturelogin.util.Hooks;
import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.util.Translate;
import me.Lythrilla.picturelogin.util.imgmessage.ImageChar;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skinsrestorer.api.property.InputDataResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PictureUtil {
    private final PictureLogin plugin;
    private ConfigManager config;
    private final Map<UUID, String[]> avatarLinesCache = new HashMap<UUID, String[]>();
    private static final long CACHE_EXPIRE_TIME = 1600000L;
    private final Map<UUID, Long> cacheLastUpdate = new HashMap<UUID, Long>();
    private final Map<String, String[]> offlinePlayerAvatarCache = new HashMap<String, String[]>();
    private final Map<String, Long> offlinePlayerCacheLastUpdate = new HashMap<String, Long>();
    private final Map<String, Boolean> loadingOfflinePlayers = new HashMap<String, Boolean>();

    public PictureUtil(PictureLogin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    private URL newURL(String player_uuid, String player_name) {
        boolean isOfflineMode;
        boolean bl = isOfflineMode = !Bukkit.getServer().getOnlineMode();
        if (Hooks.SKINSRESTORER && PictureLogin.skinsRestorerAPI != null) {
            try {
                Optional skinData = PictureLogin.skinsRestorerAPI.getSkinStorage().findSkinData(player_name);
                if (skinData.isPresent()) {
                    return new URL("https://minepic.org/avatar/8/" + ((InputDataResult)skinData.get()).getProperty().getValue());
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Error getting SkinsRestorer skin data: " + e.getMessage());
            }
        }
        String url = this.config.getURL();
        url = isOfflineMode ? ((url = url.replace("%pname%", player_name)).contains("%uuid%") && !url.contains("%pname%") ? url.replace("%uuid%", player_name) : url.replace("%uuid%", player_uuid)) : url.replace("%uuid%", player_uuid).replace("%pname%", player_name);
        try {
            return new URL(url);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Unable to parse avatar URL: " + e.getMessage());
            return null;
        }
    }

    private BufferedImage getImage(Player player) {
        String errorMsg;
        URL head_image = this.newURL(player.getUniqueId().toString(), player.getName());
        if (head_image != null) {
            try {
                HttpURLConnection connection = (HttpURLConnection)head_image.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    this.plugin.getLogger().warning(Translate.tl("error_retrieving_avatar") + " - Server returned status code: " + responseCode + " URL: " + String.valueOf(head_image));
                    return null;
                }
                return ImageIO.read(connection.getInputStream());
            }
            catch (Exception e) {
                errorMsg = e.getMessage();
                if (errorMsg == null) {
                    errorMsg = e.getClass().getName();
                }
                this.plugin.getLogger().warning(Translate.tl("error_retrieving_avatar") + " - Reason: " + errorMsg + " URL: " + String.valueOf(head_image));
            }
        } else {
            this.plugin.getLogger().warning(Translate.tl("error_avatar_url_null"));
        }
        try {
            this.plugin.getLogger().info(Translate.tl("using_fallback_img"));
            return ImageIO.read(new FallbackPicture(this.plugin).get());
        }
        catch (Exception e) {
            errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getName();
            }
            this.plugin.getLogger().warning(Translate.tl("error_fallback_img") + " - Reason: " + errorMsg);
            return null;
        }
    }

    public ImageMessage createPictureMessage(Player player, List<String> messages) {
        BufferedImage image = this.getImage(player);
        if (image == null) {
            return null;
        }
        messages.replaceAll(message -> this.addPlaceholders((String)message, player));
        return this.config.getMessage(messages, image);
    }

    public void sendOutPictureMessage(ImageMessage picture_message) {
        this.plugin.getServer().getOnlinePlayers().forEach(online_player -> {
            if (this.config.getBoolean("clear-chat", false)) {
                this.clearChat((Player)online_player);
            }
            picture_message.sendToPlayer((Player)online_player);
        });
    }

    public String addPlaceholders(String msg, Player player) {
        if (msg == null) {
            return "";
        }
        boolean isMiniMessage = msg.startsWith("<") && msg.contains(">");
        msg = msg.replace("%player%", player.getName());
        msg = msg.replace("%pname%", player.getName());
        msg = msg.replace("%uuid%", player.getUniqueId().toString());
        msg = msg.replace("%online%", String.valueOf(this.plugin.getServer().getOnlinePlayers().size()));
        msg = msg.replace("%max%", String.valueOf(this.plugin.getServer().getMaxPlayers()));
        msg = msg.replace("%motd%", this.plugin.getServer().getMotd());
        msg = msg.replace("%displayname%", player.getDisplayName());
        if (Hooks.PLACEHOLDER_API && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders((Player)player, (String)msg);
        }
        if (!isMiniMessage) {
            msg = ChatColor.translateAlternateColorCodes((char)'&', (String)msg);
        }
        return msg;
    }

    private String convertMiniMessageToLegacy(String input) {
        input = input.replace("<gray>", "&7");
        input = input.replace("</gray>", "&r");
        input = input.replace("<yellow>", "&e");
        input = input.replace("</yellow>", "&r");
        input = input.replace("<green>", "&a");
        input = input.replace("</green>", "&r");
        input = input.replace("<bold>", "&l");
        input = input.replace("</bold>", "&r");
        input = input.replace("<red>", "&c");
        input = input.replace("</red>", "&r");
        input = input.replace("<blue>", "&9");
        input = input.replace("</blue>", "&r");
        input = input.replace("<aqua>", "&b");
        input = input.replace("</aqua>", "&r");
        input = input.replace("<gold>", "&6");
        input = input.replace("</gold>", "&r");
        input = input.replace("<white>", "&f");
        input = input.replace("</white>", "&r");
        input = input.replace("<black>", "&0");
        input = input.replace("</black>", "&r");
        input = input.replaceAll("<gradient:[^>]*>", "&e");
        input = input.replace("</gradient>", "&r");
        input = input.replace("<rainbow>", "&e");
        input = input.replace("</rainbow>", "&r");
        input = ChatColor.translateAlternateColorCodes((char)'&', (String)input);
        return input;
    }

    public void clearChat(Player player) {
        for (int i = 0; i < 20; ++i) {
            player.sendMessage("");
        }
    }

    public String getAvatarLine(Player player, int lineNumber) {
        if (lineNumber < 1) {
            return "";
        }
        UUID playerUUID = player.getUniqueId();
        if (this.avatarLinesCache.containsKey(playerUUID) && System.currentTimeMillis() - this.cacheLastUpdate.getOrDefault(playerUUID, 0L) < 1600000L) {
            String[] lines = this.avatarLinesCache.get(playerUUID);
            if (lineNumber <= lines.length) {
                String line = lines[lineNumber - 1];
                return ChatColor.translateAlternateColorCodes((char)'&', (String)line);
            }
            return "";
        }
        BufferedImage image = this.getImage(player);
        if (image == null) {
            return "";
        }
        ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar());
        String[] lines = imageMessage.getLines();
        this.avatarLinesCache.put(playerUUID, lines);
        this.cacheLastUpdate.put(playerUUID, System.currentTimeMillis());
        if (lineNumber <= lines.length) {
            String line = lines[lineNumber - 1];
            return ChatColor.translateAlternateColorCodes((char)'&', (String)line);
        }
        return "";
    }

    public void clearAvatarCache(Player player) {
        UUID playerUUID = player.getUniqueId();
        this.avatarLinesCache.remove(playerUUID);
        this.cacheLastUpdate.remove(playerUUID);
    }

    public void clearAllAvatarCaches() {
        this.avatarLinesCache.clear();
        this.cacheLastUpdate.clear();
        this.offlinePlayerAvatarCache.clear();
        this.offlinePlayerCacheLastUpdate.clear();
        this.loadingOfflinePlayers.clear();
    }

    public String getAvatarLineByName(String playerName, int lineNumber) {
        Player player = Bukkit.getPlayer((String)playerName);
        if (player != null) {
            return this.getAvatarLine(player, lineNumber);
        }
        if (lineNumber < 1) {
            return "";
        }
        String lowerPlayerName = playerName.toLowerCase();
        if (this.offlinePlayerAvatarCache.containsKey(lowerPlayerName) && System.currentTimeMillis() - this.offlinePlayerCacheLastUpdate.getOrDefault(lowerPlayerName, 0L) < 1600000L) {
            String[] lines = this.offlinePlayerAvatarCache.get(lowerPlayerName);
            if (lineNumber <= lines.length) {
                String line = lines[lineNumber - 1];
                return ChatColor.translateAlternateColorCodes((char)'&', (String)line);
            }
            return "";
        }
        if (Boolean.TRUE.equals(this.loadingOfflinePlayers.get(lowerPlayerName))) {
            return "&7Loading...";
        }
        this.loadingOfflinePlayers.put(lowerPlayerName, true);
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                BufferedImage image;
                block13: {
                    URL head_image = this.newURL(playerName, playerName);
                    image = null;
                    if (head_image != null) {
                        try {
                            HttpURLConnection connection = (HttpURLConnection)head_image.openConnection();
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            int responseCode = connection.getResponseCode();
                            if (responseCode != 200) {
                                this.plugin.getLogger().warning(Translate.tl("error_retrieving_avatar") + " - Server returned status code: " + responseCode + " Player: " + playerName + " URL: " + String.valueOf(head_image));
                                break block13;
                            }
                            image = ImageIO.read(connection.getInputStream());
                        }
                        catch (Exception e) {
                            String errorMsg = e.getMessage();
                            if (errorMsg == null) {
                                errorMsg = e.getClass().getName();
                            }
                            this.plugin.getLogger().warning(Translate.tl("error_retrieving_avatar") + " - Reason: " + errorMsg + " Player: " + playerName + " URL: " + String.valueOf(head_image));
                        }
                    } else {
                        this.plugin.getLogger().warning(Translate.tl("error_avatar_url_null") + " Player: " + playerName);
                    }
                }
                if (image == null) {
                    try {
                        this.plugin.getLogger().info(Translate.tl("using_fallback_img") + " Player: " + playerName);
                        image = ImageIO.read(new FallbackPicture(this.plugin).get());
                    }
                    catch (Exception e) {
                        String errorMsg = e.getMessage();
                        if (errorMsg == null) {
                            errorMsg = e.getClass().getName();
                        }
                        this.plugin.getLogger().warning(Translate.tl("error_fallback_img") + " - Reason: " + errorMsg + " Player: " + playerName);
                        this.loadingOfflinePlayers.remove(lowerPlayerName);
                        return;
                    }
                }
                ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar());
                String[] lines = imageMessage.getLines();
                this.offlinePlayerAvatarCache.put(lowerPlayerName, lines);
                this.offlinePlayerCacheLastUpdate.put(lowerPlayerName, System.currentTimeMillis());
                this.loadingOfflinePlayers.remove(lowerPlayerName);
            }
            catch (Exception e) {
                this.loadingOfflinePlayers.remove(lowerPlayerName);
                String errorMsg = e.getMessage();
                if (errorMsg == null) {
                    errorMsg = e.getClass().getName();
                }
                this.plugin.getLogger().warning("Failed to get avatar for player " + playerName + ": " + errorMsg);
            }
        });
        return "&7Loading...";
    }
}
