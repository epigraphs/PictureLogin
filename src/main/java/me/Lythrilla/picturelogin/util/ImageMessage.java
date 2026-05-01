package me.Lythrilla.picturelogin.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;
import me.Lythrilla.picturelogin.PictureLogin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.themoep.minedown.MineDown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ImageMessage {
    private static final char TRANSPARENT_CHAR = ' ';
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().character('&').hexCharacter('#').useUnusualXRepeatedCharacterHexFormat().build();
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#([0-9A-F]{6})");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("(?i)&([0-9A-FK-ORX])");
    private String[] lines;

    public ImageMessage(BufferedImage image, int height, char imgChar) {
        Color[][] chatColors = this.toChatColorArray(image, height);
        this.lines = this.toImgMessage(chatColors, imgChar);
    }

    private Color[][] toChatColorArray(BufferedImage image, int height) {
        double ratio = (double)image.getHeight() / (double)image.getWidth();
        int width = (int)((double)height / ratio);
        if (width > 10) {
            width = 10;
        }
        BufferedImage resized = this.resizeImage(image, width, height);
        Color[][] chatImg = new Color[resized.getWidth()][resized.getHeight()];
        for (int x = 0; x < resized.getWidth(); ++x) {
            for (int y = 0; y < resized.getHeight(); ++y) {
                int rgb = resized.getRGB(x, y);
                chatImg[x][y] = new Color(rgb, true);
            }
        }
        return chatImg;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        AffineTransform af = new AffineTransform();
        af.scale((double)width / (double)originalImage.getWidth(), (double)height / (double)originalImage.getHeight());
        AffineTransformOp operation = new AffineTransformOp(af, 1);
        return operation.filter(originalImage, null);
    }

    private String[] toImgMessage(Color[][] colors, char imgchar) {
        this.lines = new String[colors[0].length];
        for (int y = 0; y < colors[0].length; ++y) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < colors.length; ++x) {
                Color color = colors[x][y];
                if (color != null) {
                    line.append("&").append(this.colorToHex(colors[x][y])).append(imgchar);
                    continue;
                }
                line.append(' ');
            }
            this.lines[y] = line.toString() + String.valueOf(ChatColor.RESET);
        }
        return this.lines;
    }

    private String colorToHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public ImageMessage appendText(String ... text) {
        for (int y = 0; y < this.lines.length; ++y) {
            if (text.length <= y) continue;
            int n = y;
            this.lines[n] = this.lines[n] + " " + text[y];
        }
        return this;
    }

    public ImageMessage appendCenteredText(String ... text) {
        for (int y = 0; y < this.lines.length; ++y) {
            if (text.length <= y) {
                return this;
            }
            int len = 65 - this.lines[y].length();
            this.lines[y] = this.lines[y] + this.center(text[y], len);
        }
        return this;
    }

    private String center(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        }
        if (s.length() == length) {
            return s;
        }
        int leftPadding = (length - s.length()) / 2;
        StringBuilder leftBuilder = new StringBuilder();
        for (int i = 0; i < leftPadding; ++i) {
            leftBuilder.append(" ");
        }
        return leftBuilder.toString() + s;
    }

    private String legacyToMiniMessage(String input) {
        String result = HEX_COLOR_PATTERN.matcher(input).replaceAll(matchResult -> {
            String hex = matchResult.group(1);
            return "<#" + hex + ">";
        });
        result = AMPERSAND_PATTERN.matcher(result).replaceAll(matchResult -> {
            String code;
            switch (code = matchResult.group(1).toLowerCase()) {
                case "0": {
                    return "<black>";
                }
                case "1": {
                    return "<dark_blue>";
                }
                case "2": {
                    return "<dark_green>";
                }
                case "3": {
                    return "<dark_aqua>";
                }
                case "4": {
                    return "<dark_red>";
                }
                case "5": {
                    return "<dark_purple>";
                }
                case "6": {
                    return "<gold>";
                }
                case "7": {
                    return "<gray>";
                }
                case "8": {
                    return "<dark_gray>";
                }
                case "9": {
                    return "<blue>";
                }
                case "a": {
                    return "<green>";
                }
                case "b": {
                    return "<aqua>";
                }
                case "c": {
                    return "<red>";
                }
                case "d": {
                    return "<light_purple>";
                }
                case "e": {
                    return "<yellow>";
                }
                case "f": {
                    return "<white>";
                }
                case "k": {
                    return "<obfuscated>";
                }
                case "l": {
                    return "<bold>";
                }
                case "m": {
                    return "<strikethrough>";
                }
                case "n": {
                    return "<underlined>";
                }
                case "o": {
                    return "<italic>";
                }
                case "r": {
                    return "<reset>";
                }
            }
            return "&" + code;
        });
        return result;
    }

    public void sendToPlayer(Player player) {
        try {
            PictureLogin plugin = (PictureLogin)Bukkit.getPluginManager().getPlugin("PictureLogin");
            if (plugin == null) {
                this.sendToPlayerLegacy(player);
                return;
            }
            for (String line : this.lines) {
                Component finalComponent;
                String[] parts = line.split(" ", 2);
                String avatarPart = parts[0];
                TextComponent avatarComponent = LEGACY_SERIALIZER.deserialize(ChatColor.translateAlternateColorCodes((char)'&', (String)avatarPart));
                if (parts.length > 1) {
                    Object textComponent;
                    String textPart = parts[1];
                    if (this.isMiniMessageFormat(textPart)) {
                        textComponent = MINI_MESSAGE.deserialize(textPart);
                    } else {
                        String miniMessageText = this.legacyToMiniMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)textPart));
                        textComponent = MINI_MESSAGE.deserialize(miniMessageText);
                    }
                    finalComponent = ((TextComponent)((TextComponent)Component.empty().append(avatarComponent)).append(Component.space())).append((Component)textComponent);
                } else {
                    finalComponent = avatarComponent;
                }
                plugin.adventure().player(player).sendMessage(finalComponent);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.sendToPlayerLegacy(player);
        }
    }

    private void sendToPlayerLegacy(Player player) {
        for (String line : this.lines) {
            player.spigot().sendMessage(MineDown.parse(line, new String[0]));
        }
    }

    private boolean isMiniMessageFormat(String text) {
        return text.contains("<") && text.contains(">") && (text.contains("<gray>") || text.contains("<yellow>") || text.contains("<green>") || text.contains("<rainbow>") || text.contains("<gradient") || text.contains("<#"));
    }

    public String[] getLines() {
        return this.lines;
    }
}
