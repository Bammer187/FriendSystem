package me.yinzuro.friendSystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class RespondCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player.");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /respond <message>");
            return true;
        }

        return false;
    }

    private UUID getLastFriendMessagedUUID(Player player) throws SQLException {

    }
}
