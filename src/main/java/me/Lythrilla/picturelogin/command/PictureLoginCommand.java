package me.Lythrilla.picturelogin.command;

import java.util.ArrayList;
import java.util.List;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.message.MessageService;
import me.Lythrilla.picturelogin.util.Translate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PictureLoginCommand
implements CommandExecutor {
    private final PictureLogin plugin;
    private final UserManager userManager;
    private final MessageService messageService;

    public PictureLoginCommand(PictureLogin plugin, UserManager userManager, MessageService messageService) {
        this.plugin = plugin;
        this.userManager = userManager;
        this.messageService = messageService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand;
        if (args.length == 0) {
            this.showHelp(sender);
            return true;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "reload": {
                if (!sender.hasPermission("picturelogin.reload")) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("no_permission"));
                    return true;
                }
                this.plugin.reloadPlugin();
                sender.sendMessage(String.valueOf(ChatColor.GREEN) + Translate.tl("config_reloaded"));
                return true;
            }
            case "set": {
                if (args.length < 2) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("command_usage", "/picturelogin set <login|firstjoin|leave> [\u73a9\u5bb6\u540d]"));
                    return true;
                }
                if (!sender.hasPermission("picturelogin.set")) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("no_permission"));
                    return true;
                }
                this.handleSetCommand(sender, args);
                return true;
            }
            case "clear": {
                if (args.length < 2) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("command_usage", "/picturelogin clear <login|firstjoin|leave> [\u73a9\u5bb6\u540d]"));
                    return true;
                }
                if (!sender.hasPermission("picturelogin.clear")) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("no_permission"));
                    return true;
                }
                this.handleClearCommand(sender, args);
                return true;
            }
        }
        this.showHelp(sender);
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "PictureLogin \u547d\u4ee4\u5e2e\u52a9:");
        sender.sendMessage(String.valueOf(ChatColor.GRAY) + "/picturelogin reload - " + String.valueOf(ChatColor.WHITE) + "\u91cd\u65b0\u52a0\u8f7d\u914d\u7f6e\u6587\u4ef6");
        sender.sendMessage(String.valueOf(ChatColor.GRAY) + "/picturelogin set <login|firstjoin|leave> [\u73a9\u5bb6\u540d] - " + String.valueOf(ChatColor.WHITE) + "\u8bbe\u7f6e\u81ea\u5b9a\u4e49\u6d88\u606f");
        sender.sendMessage(String.valueOf(ChatColor.GRAY) + "/picturelogin clear <login|firstjoin|leave> [\u73a9\u5bb6\u540d] - " + String.valueOf(ChatColor.WHITE) + "\u6e05\u9664\u81ea\u5b9a\u4e49\u6d88\u606f");
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        String messageType;
        String type = args[1].toLowerCase();
        Player targetPlayer = null;
        if (args.length >= 3) {
            String playerName = args[2];
            targetPlayer = this.plugin.getServer().getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("player_not_found", playerName));
                return;
            }
        } else if (sender instanceof Player) {
            targetPlayer = (Player)sender;
        } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("command_usage", "/picturelogin set <login|firstjoin|leave> <\u73a9\u5bb6\u540d>"));
            return;
        }
        switch (type) {
            case "login": {
                messageType = "messages";
                break;
            }
            case "firstjoin": {
                messageType = "first-join-messages";
                break;
            }
            case "leave": {
                messageType = "leave-messages";
                break;
            }
            default: {
                sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("command_usage", "/picturelogin set <login|firstjoin|leave> [\u73a9\u5bb6\u540d]"));
                return;
            }
        }
        List<String> currentMessages = this.getCurrentMessages(type, targetPlayer);
        sender.sendMessage(String.valueOf(ChatColor.GREEN) + Translate.tl("messages_set", targetPlayer.getName(), type));
        for (String line : currentMessages) {
            sender.sendMessage(String.valueOf(ChatColor.GRAY) + "- " + String.valueOf(ChatColor.WHITE) + line);
        }
        this.userManager.setPlayerMessages(targetPlayer, currentMessages, messageType);
    }

    private List<String> getCurrentMessages(String type, Player player) {
        switch (type) {
            case "login": {
                return this.messageService.getLoginMessages(player);
            }
            case "firstjoin": {
                return this.messageService.getFirstJoinMessages(player);
            }
            case "leave": {
                return this.messageService.getLeaveMessages(player);
            }
        }
        return new ArrayList<String>();
    }

    private void handleClearCommand(CommandSender sender, String[] args) {
        String messageType;
        String type = args[1].toLowerCase();
        Player targetPlayer = null;
        if (args.length >= 3) {
            String playerName = args[2];
            targetPlayer = this.plugin.getServer().getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("player_not_found", playerName));
                return;
            }
        } else if (sender instanceof Player) {
            targetPlayer = (Player)sender;
        } else {
            sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("command_usage", "/picturelogin clear <login|firstjoin|leave> <\u73a9\u5bb6\u540d>"));
            return;
        }
        switch (type) {
            case "login": {
                messageType = "messages";
                break;
            }
            case "firstjoin": {
                messageType = "first-join-messages";
                break;
            }
            case "leave": {
                messageType = "leave-messages";
                break;
            }
            default: {
                sender.sendMessage(String.valueOf(ChatColor.RED) + Translate.tl("command_usage", "/picturelogin clear <login|firstjoin|leave> [\u73a9\u5bb6\u540d]"));
                return;
            }
        }
        this.userManager.setPlayerMessages(targetPlayer, new ArrayList<String>(), messageType);
        sender.sendMessage(String.valueOf(ChatColor.GREEN) + Translate.tl("messages_cleared", targetPlayer.getName(), type));
    }
}
