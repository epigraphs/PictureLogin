package me.Lythrilla.picturelogin.commands;

import java.util.List;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.util.ImageMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand {
    private final PictureLogin plugin;

    public DebugCommand(PictureLogin plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picturelogin.debug")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_command_help")));
            return true;
        }
        String messageType = args[1].toLowerCase();
        if (!this.isValidMessageType(messageType)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_invalid_type").replace("%type%", messageType)));
            return true;
        }
        if (sender instanceof Player && this.plugin.getConfigManager().getBoolean("clear-chat", false)) {
            this.plugin.getPictureUtil().clearChat((Player)sender);
        }
        String targetType = "global";
        if (args.length >= 3) {
            targetType = args[2].toLowerCase();
        }
        switch (targetType) {
            case "global": {
                this.previewGlobalMessages(sender, messageType);
                break;
            }
            case "user": {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_command_help")));
                    return true;
                }
                this.previewUserMessages(sender, messageType, args[3]);
                break;
            }
            case "perm": {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_command_help")));
                    return true;
                }
                this.previewPermissionMessages(sender, messageType, args[3]);
                break;
            }
            default: {
                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_command_help")));
            }
        }
        return true;
    }

    private boolean isValidMessageType(String type) {
        return type.equals("login") || type.equals("leave") || type.equals("firstjoin") || type.equals("all");
    }

    private void previewGlobalMessages(CommandSender sender, String messageType) {
        List<String> messages = null;
        String debugHeader = this.plugin.getConfigManager().getMessage("debug_message_header").replace("%type%", this.getTypeName(messageType)).replace("%target%", "\u5168\u5c40");
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)debugHeader));
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (messageType.equals("login") || messageType.equals("all")) {
                messages = this.plugin.getConfigManager().getMessages();
                this.previewMessageWithAvatar(player, messages, "\u767b\u5f55");
            }
            if (messageType.equals("firstjoin") || messageType.equals("all")) {
                messages = this.plugin.getConfigManager().getFirstJoinMessages();
                this.previewMessageWithAvatar(player, messages, "\u9996\u6b21\u52a0\u5165");
            }
            if (messageType.equals("leave") || messageType.equals("all")) {
                messages = this.plugin.getConfigManager().getLeaveMessages();
                this.previewMessageWithAvatar(player, messages, "\u79bb\u5f00");
            }
        } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u6267\u884c\u4ee5\u67e5\u770b\u5b8c\u6574\u6548\u679c");
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_message_footer")));
    }

    private void previewUserMessages(CommandSender sender, String messageType, String playerName) {
        Player targetPlayer = Bukkit.getPlayerExact((String)playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_user_not_found").replace("%player%", playerName)));
            return;
        }
        UserManager.UserSettings userSettings = this.plugin.getUserManager().getUserSettings(targetPlayer);
        if (userSettings == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_user_not_found").replace("%player%", playerName)));
            return;
        }
        String debugHeader = this.plugin.getConfigManager().getMessage("debug_message_header").replace("%type%", this.getTypeName(messageType)).replace("%target%", playerName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)debugHeader));
        if (sender instanceof Player) {
            PermissionManager.PermissionGroup group;
            List<String> messages;
            Player player = (Player)sender;
            if (messageType.equals("login") || messageType.equals("all")) {
                messages = userSettings.getMessages();
                if (messages.isEmpty() && this.plugin.getConfigManager().getBoolean("enable-permission-messages", true) && (group = this.plugin.getPermissionManager().getPlayerPermissionGroup(targetPlayer)) != null && group.hasCustomMessages()) {
                    messages = group.getMessages();
                }
                if (messages.isEmpty()) {
                    messages = this.plugin.getConfigManager().getMessages();
                }
                this.previewMessageWithAvatar(player, messages, "\u767b\u5f55 (" + playerName + ")", targetPlayer);
            }
            if (messageType.equals("firstjoin") || messageType.equals("all")) {
                messages = userSettings.getFirstJoinMessages();
                if (messages.isEmpty() && this.plugin.getConfigManager().getBoolean("enable-permission-messages", true) && (group = this.plugin.getPermissionManager().getPlayerPermissionGroup(targetPlayer)) != null && group.hasCustomFirstJoinMessages()) {
                    messages = group.getFirstJoinMessages();
                }
                if (messages.isEmpty()) {
                    messages = this.plugin.getConfigManager().getFirstJoinMessages();
                }
                this.previewMessageWithAvatar(player, messages, "\u9996\u6b21\u52a0\u5165 (" + playerName + ")", targetPlayer);
            }
            if (messageType.equals("leave") || messageType.equals("all")) {
                messages = userSettings.getLeaveMessages();
                if (messages.isEmpty() && this.plugin.getConfigManager().getBoolean("enable-permission-messages", true) && (group = this.plugin.getPermissionManager().getPlayerPermissionGroup(targetPlayer)) != null && group.hasCustomLeaveMessages()) {
                    messages = group.getLeaveMessages();
                }
                if (messages.isEmpty()) {
                    messages = this.plugin.getConfigManager().getLeaveMessages();
                }
                this.previewMessageWithAvatar(player, messages, "\u79bb\u5f00 (" + playerName + ")", targetPlayer);
            }
        } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u6267\u884c\u4ee5\u67e5\u770b\u5b8c\u6574\u6548\u679c");
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_message_footer")));
    }

    private void previewPermissionMessages(CommandSender sender, String messageType, String groupName) {
        List<PermissionManager.PermissionGroup> groups = this.plugin.getPermissionManager().getPermissionGroups();
        PermissionManager.PermissionGroup targetGroup = null;
        for (PermissionManager.PermissionGroup group : groups) {
            if (!group.getName().equalsIgnoreCase(groupName)) continue;
            targetGroup = group;
            break;
        }
        if (targetGroup == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_group_not_found").replace("%group%", groupName)));
            return;
        }
        String debugHeader = this.plugin.getConfigManager().getMessage("debug_message_header").replace("%type%", this.getTypeName(messageType)).replace("%target%", "\u6743\u9650\u7ec4: " + targetGroup.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)debugHeader));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_permission_message").replace("%group%", targetGroup.getName()).replace("%permission%", targetGroup.getPermission()).replace("%priority%", String.valueOf(targetGroup.getPriority()))));
        if (sender instanceof Player) {
            List<String> messages;
            Player player = (Player)sender;
            if (messageType.equals("login") || messageType.equals("all")) {
                messages = targetGroup.getMessages();
                if (messages.isEmpty()) {
                    messages = this.plugin.getConfigManager().getMessages();
                }
                this.previewMessageWithAvatar(player, messages, "\u767b\u5f55 (\u6743\u9650\u7ec4: " + targetGroup.getName() + ")");
            }
            if (messageType.equals("firstjoin") || messageType.equals("all")) {
                messages = targetGroup.getFirstJoinMessages();
                if (messages.isEmpty()) {
                    messages = this.plugin.getConfigManager().getFirstJoinMessages();
                }
                this.previewMessageWithAvatar(player, messages, "\u9996\u6b21\u52a0\u5165 (\u6743\u9650\u7ec4: " + targetGroup.getName() + ")");
            }
            if (messageType.equals("leave") || messageType.equals("all")) {
                messages = targetGroup.getLeaveMessages();
                if (messages.isEmpty()) {
                    messages = this.plugin.getConfigManager().getLeaveMessages();
                }
                this.previewMessageWithAvatar(player, messages, "\u79bb\u5f00 (\u6743\u9650\u7ec4: " + targetGroup.getName() + ")");
            }
        } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u6267\u884c\u4ee5\u67e5\u770b\u5b8c\u6574\u6548\u679c");
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("debug_message_footer")));
    }

    private void previewMessageWithAvatar(Player player, List<String> messages, String typeLabel) {
        this.previewMessageWithAvatar(player, messages, typeLabel, player);
    }

    private void previewMessageWithAvatar(Player player, List<String> messages, String typeLabel, Player targetPlayer) {
        if (messages == null || messages.isEmpty()) {
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "  (" + typeLabel + "\u65e0\u6d88\u606f\u914d\u7f6e)");
            return;
        }
        player.sendMessage(String.valueOf(ChatColor.GOLD) + "\u25b6 " + typeLabel + "\u6d88\u606f\u9884\u89c8:");
        ImageMessage imageMessage = this.plugin.getPictureUtil().createPictureMessage(targetPlayer, messages);
        if (imageMessage != null) {
            imageMessage.sendToPlayer(player);
        } else {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u65e0\u6cd5\u521b\u5efa\u5934\u50cf\u56fe\u7247\u6d88\u606f\uff0c\u8bf7\u68c0\u67e5\u914d\u7f6e");
        }
    }

    private String getTypeName(String type) {
        switch (type) {
            case "login": {
                return this.plugin.getConfigManager().getMessage("message_type_login");
            }
            case "firstjoin": {
                return this.plugin.getConfigManager().getMessage("message_type_firstjoin");
            }
            case "leave": {
                return this.plugin.getConfigManager().getMessage("message_type_leave");
            }
            case "all": {
                return "\u6240\u6709\u6d88\u606f";
            }
        }
        return type;
    }
}
