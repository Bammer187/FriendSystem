package me.yinzuro.friendSystem.commands;

import static me.yinzuro.friendSystem.utils.ChatPrefix.PREFIX;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class FriendCommandExecutor implements CommandExecutor {

    private final FriendAddCommandExecutor addExecutor = new FriendAddCommandExecutor();
    private final FriendAcceptCommandExecutor acceptExecutor = new FriendAcceptCommandExecutor();
    private final FriendDenyCommandExecutor denyExecutor = new FriendDenyCommandExecutor();
    private final FriendRemoveCommandExecutor removeExecutor = new FriendRemoveCommandExecutor();
    private final FriendListCommandExecutor listExecutor = new FriendListCommandExecutor();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(PREFIX + "Usage: /friend <add|remove|list|accept|deny> <player>");
            return true;
        }

        String subcommand = strings[0].toLowerCase();
        String[] subStrings = new String[strings.length - 1];
        System.arraycopy(strings, 1, subStrings, 0, subStrings.length);

        switch (subcommand) {
            case "add": {
                return addExecutor.onCommand(commandSender, command, s, subStrings);
            }
            case "accept": {
                return acceptExecutor.onCommand(commandSender, command, s, subStrings);
            }
            case "deny": {
                return denyExecutor.onCommand(commandSender, command, s, subStrings);
            }
            case "remove": {
                return removeExecutor.onCommand(commandSender, command, s, subStrings);
            }
            case "list" : {
                return listExecutor.onCommand(commandSender, command, s, subStrings);
            }
            default: {
                commandSender.sendMessage(PREFIX + "Unknown command.");
                return true;
            }
        }
    }
}
