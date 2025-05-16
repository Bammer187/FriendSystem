package me.yinzuro.friendSystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class FriendCommandExecutor implements CommandExecutor {

    private final FriendAddCommandExecutor addExecutor = new FriendAddCommandExecutor();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage("§cUsage: /friend <add|remove|list> <player>");
            return true;
        }

        String subcommand = strings[0].toLowerCase();

        switch (subcommand) {
            case "add":
                String[] subStrings = new String[strings.length - 1];
                System.arraycopy(strings, 1, subStrings, 0, subStrings.length);
                return addExecutor.onCommand(commandSender, command, s, subStrings);

            default:
                commandSender.sendMessage("§cUnknown command.");
                return true;
        }
    }
}
