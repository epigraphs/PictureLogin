package me.Lythrilla.picturelogin.commands;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.commands.DebugCommand;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BaseCommand
implements CommandExecutor {
    private final PictureLogin plugin;

    public BaseCommand(PictureLogin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String subCommand;
        if (args.length == 0 || args.length == 1 && args[0].equalsIgnoreCase("help")) {
            this.showHelp(sender);
            return true;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "reload": {
                if (!sender.hasPermission("picturelogin.reload")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("no_permission")));
                    return true;
                }
                this.plugin.getConfigManager().reloadConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("reload_config")));
                return true;
            }
            case "version": {
                String versionInfo = this.plugin.getConfigManager().getMessage("version_info").replace("%version%", this.plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)versionInfo));
                if (this.plugin.getConfigManager().getBoolean("update-check", true)) {
                    // empty if block
                }
                return true;
            }
            case "language": {
                this.handleLanguageCommand(sender, args);
                return true;
            }
            case "debug": {
                if (!this.plugin.getConfig().getBoolean("settings.enable_commands.debug", true)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("command_disabled")));
                    return true;
                }
                DebugCommand debugCommand = new DebugCommand(this.plugin);
                return debugCommand.execute(sender, args);
            }
        }
        this.showHelp(sender);
        return true;
    }

    private void showHelp(CommandSender sender) {
        String prefix = this.plugin.getConfigManager().getMessage("prefix");
        String helpHeader = this.plugin.getConfigManager().getMessage("command_help_header");
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)helpHeader));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)(prefix + this.plugin.getConfigManager().getMessage("command_version_syntax") + " - " + this.plugin.getConfigManager().getMessage("command_version_desc"))));
        if (sender.hasPermission("picturelogin.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)(prefix + this.plugin.getConfigManager().getMessage("command_reload_syntax") + " - " + this.plugin.getConfigManager().getMessage("command_reload_desc"))));
        }
        if (sender.hasPermission("picturelogin.language")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)(prefix + this.plugin.getConfigManager().getMessage("command_language_syntax") + " - " + this.plugin.getConfigManager().getMessage("command_language_desc"))));
        }
        if (sender.hasPermission("picturelogin.debug")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)(prefix + this.plugin.getConfigManager().getMessage("command_debug_syntax") + " - " + this.plugin.getConfigManager().getMessage("command_debug_desc"))));
        }
    }

    private void handleLanguageCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("picturelogin.language")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.plugin.getConfigManager().getMessage("no_permission")));
            return;
        }
        LanguageManager languageManager = this.plugin.getConfigManager().getLanguageManager();
        if (args.length == 1) {
            ConfigManager configManager = this.plugin.getConfigManager();
            String header = ChatColor.translateAlternateColorCodes((char)'&', (String)configManager.getMessage("language_list_header"));
            String currentLang = ChatColor.translateAlternateColorCodes((char)'&', (String)configManager.getMessage("current_language").replace("%language%", languageManager.getCurrentLanguage()));
            String availableLangs = ChatColor.translateAlternateColorCodes((char)'&', (String)configManager.getMessage("available_languages").replace("%languages%", String.join((CharSequence)", ", languageManager.getAvailableLanguages())));
            String usage = ChatColor.translateAlternateColorCodes((char)'&', (String)configManager.getMessage("language_usage"));
            String footer = ChatColor.translateAlternateColorCodes((char)'&', (String)configManager.getMessage("language_list_footer"));
            sender.sendMessage(header);
            sender.sendMessage(currentLang);
            sender.sendMessage(availableLangs);
            sender.sendMessage(usage);
            sender.sendMessage(footer);
            return;
        }
        if (args.length == 2) {
            String newLang = args[1];
            if (!languageManager.getAvailableLanguages().contains(newLang)) {
                String invalidLangMsg = this.plugin.getConfigManager().getMessage("invalid_language").replace("%language%", newLang).replace("%languages%", String.join((CharSequence)", ", languageManager.getAvailableLanguages()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)invalidLangMsg));
                return;
            }
            String messageTemplate = this.plugin.getConfigManager().getMessage("language_changed");
            languageManager.setLanguage(newLang);
            this.plugin.reloadPlugin();
            String message = messageTemplate.replace("%language%", newLang);
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
            String currentVersionInfo = this.plugin.getConfigManager().getMessage("version_info").replace("%version%", this.plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)currentVersionInfo));
            return;
        }
        String usageMessage = this.plugin.getConfigManager().getMessage("command_usage").replace("%s", "/picturelogin language <\u8bed\u8a00\u4ee3\u7801>");
        sender.sendMessage(usageMessage);
    }
}
