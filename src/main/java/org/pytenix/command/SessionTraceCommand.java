package org.pytenix.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.pytenix.SessionTracePlugin;
import org.pytenix.services.WhitelistService;


public class SessionTraceCommand implements CommandExecutor {

    private final SessionTracePlugin plugin;
    private final LegacyComponentSerializer legacyComponentSerializer;

    public SessionTraceCommand(SessionTracePlugin plugin) {
        this.plugin = plugin;
        this.legacyComponentSerializer = LegacyComponentSerializer.legacySection();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (command.getName().equalsIgnoreCase("trace")) {
            if (strings.length == 0) {
                sendHelp(commandSender);
                return true;
            }

            String subCommand = strings[0].toLowerCase();
            switch (subCommand) {
                case "reload":
                    return handleReloadConfigCommand(commandSender);
                case "whitelist":

                    if (strings.length == 1) {
                        handleWhitelistCommand(commandSender);
                        return true;
                    }

                    if (strings.length == 3) {

                        String whitelistArgument = strings[1].toLowerCase();
                        String name = strings[2].toLowerCase();

                        switch (whitelistArgument) {
                            case "add" -> handleUpdateWhitelistCommand(commandSender, name, true);
                            case "remove" -> handleUpdateWhitelistCommand(commandSender, name, false);
                            default -> sendHelp(commandSender);
                        }
                        return true;

                    }


                default:
                    sendHelp(commandSender);
                    return true;
            }
        }


        return true;
    }


    public void handleUpdateWhitelistCommand(CommandSender commandSender, String name, boolean add) {
        plugin.getWhitelistService().modifyWhitelist(name.toLowerCase(), add).whenComplete((result, throwable) ->
        {
            if (checkForError(throwable, commandSender))
                return;

            if (result == WhitelistService.Result.ERROR) {
                sendMessage(commandSender, "An error occurred while modifying the whitelist!", Color.RED);
                return;
            }

            if (result == WhitelistService.Result.DUPLICATE || result == WhitelistService.Result.NOT_EXISTS) {
                sendMessage(commandSender, "This user is " + (add ? "already" : "is not") + " whitelisted!", Color.RED);
                return;
            }

            sendMessage(commandSender, "Successfully " + (add ? "added" : "removed") + " " + name + " " + (add ? "to" : "from") + " the whitelist", Color.GREEN);
        });
    }

    public boolean handleReloadConfigCommand(CommandSender sender) {
        int oldLimit = plugin.getConfigService().getConfiguration().getMaxAccountsPerIp();

        plugin.getConfigService().reloadConfig();

        int newLimit = plugin.getConfigService().getConfiguration().getMaxAccountsPerIp();

        if (newLimit < oldLimit) {
            sendMessage(sender, "Limit decreased (" + oldLimit + " -> " + newLimit + "). Cleaning up Database & Cache...", Color.YELLOW);

            plugin.getSessionTraceService().resetAndReinitialize(() -> sendMessage(sender, "Reset completed: Current Players refreshed", Color.GREEN));

        } else {
            sendMessage(sender, "Config successfully reloaded", Color.GREEN);
        }

        return true;

    }

    public void handleWhitelistCommand(CommandSender sender) {
        plugin.getPlayerDatabase().getWhitelist().whenComplete((userNames, throwable) ->
        {
            if (checkForError(throwable, sender))
                return;

            if (userNames.isEmpty()) {
                sendMessage(sender, "There are currently no whitelisted users!", Color.RED);
                return;
            }
            sendMessage(sender, "There are currently " + userNames.size() + " whitelisted users", Color.YELLOW);
            for (String userName : userNames) {
                ClickEvent clickEvent = ClickEvent.runCommand("/trace whitelist remove " + userName);
                sendMessage(sender, Component.text(userName).color(toColor(Color.GREEN))
                        .append(Component.text(" - ").color(toColor(Color.GRAY)))
                        .append(Component.text("[DELETE]").clickEvent(clickEvent).color(toColor(Color.RED))));
            }


        });
    }

    public boolean checkForError(Throwable throwable, CommandSender commandSender) {
        if (throwable != null) {
            sendMessage(commandSender, "An error occured! " + throwable.getMessage(), Color.RED);
            throwable.printStackTrace();
            return true;
        }
        return false;
    }


    public void sendHelp(CommandSender sender) {
        sendMessage(sender, "/trace reload - Reloads the config", Color.GREEN);
        sendMessage(sender, "/trace whitelist - Shows all whitelisted names", Color.GREEN);
        sendMessage(sender, "/trace whitelist add <Name> - Adds a specific name to the whitelist", Color.GREEN);
        sendMessage(sender, "/trace whitelist remove <Name> - Removes a specific name to the whitelist", Color.GREEN);
    }

    public TextColor toColor(Color color) {
        return TextColor.color(color.asRGB());
    }


    public Component getPrefix() {
        return legacyComponentSerializer.deserialize(plugin.getConfigService().getConfiguration().getPrefix() + " ");
    }

    public void sendMessage(CommandSender sender, Component message) {
        sender.sendMessage(getPrefix().append(message));
    }

    public void sendMessage(CommandSender sender, String message, Color color) {
        sender.sendMessage(getPrefix().append(Component.text(message).color(toColor(color))));
    }
}
