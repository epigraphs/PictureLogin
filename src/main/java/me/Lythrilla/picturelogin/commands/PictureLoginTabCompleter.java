package me.Lythrilla.picturelogin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class PictureLoginTabCompleter
implements TabCompleter {
    private final PictureLogin plugin;
    private final List<String> COMMANDS = Arrays.asList("reload", "version", "language", "help", "debug");
    private final List<String> DEBUG_TYPES = Arrays.asList("login", "leave", "firstjoin", "all");
    private final List<String> DEBUG_TARGETS = Arrays.asList("global", "user", "perm");

    public PictureLoginTabCompleter(PictureLogin plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            ArrayList<String> availableCommands = new ArrayList<String>();
            for (String cmd : this.COMMANDS) {
                if (!this.hasPermission(sender, cmd)) continue;
                availableCommands.add(cmd);
            }
            return this.getMatchingCompletions(availableCommands, args[0]);
        }
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("language") && sender.hasPermission("picturelogin.language")) {
                List<String> languages = this.plugin.getConfigManager().getLanguageManager().getAvailableLanguages();
                return this.getMatchingCompletions(languages, args[1]);
            }
            if (subCommand.equals("debug") && sender.hasPermission("picturelogin.debug")) {
                return this.getMatchingCompletions(this.DEBUG_TYPES, args[1]);
            }
        }
        if (args.length >= 3 && args[0].toLowerCase().equals("debug") && sender.hasPermission("picturelogin.debug")) {
            if (args.length == 3) {
                return this.getMatchingCompletions(this.DEBUG_TARGETS, args[2]);
            }
            if (args.length == 4) {
                if (args[2].toLowerCase().equals("user")) {
                    ArrayList<String> playerNames = new ArrayList<String>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerNames.add(player.getName());
                    }
                    return this.getMatchingCompletions(playerNames, args[3]);
                }
                if (args[2].toLowerCase().equals("perm")) {
                    ArrayList<String> groupNames = new ArrayList<String>();
                    for (PermissionManager.PermissionGroup group : this.plugin.getPermissionManager().getPermissionGroups()) {
                        groupNames.add(group.getName());
                    }
                    return this.getMatchingCompletions(groupNames, args[3]);
                }
            }
        }
        return completions;
    }

    private boolean hasPermission(CommandSender sender, String command) {
        switch (command) {
            case "reload": {
                return sender.hasPermission("picturelogin.reload");
            }
            case "language": {
                return sender.hasPermission("picturelogin.language");
            }
            case "debug": {
                return sender.hasPermission("picturelogin.debug");
            }
            case "version": 
            case "help": {
                return true;
            }
        }
        return false;
    }

    private List<String> getMatchingCompletions(List<String> options, String input) {
        if (input.isEmpty()) {
            return options;
        }
        String lowerInput = input.toLowerCase();
        List<String> result = options.stream().filter(option -> option.toLowerCase().startsWith(lowerInput)).collect(Collectors.toList());
        Collections.sort(result);
        return result;
    }
}
