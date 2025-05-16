package me.yinzuro.friendSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FriendAddCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend add <Player>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou can't add yourself.");
            return true;
        }

        player.sendMessage("§aSend friend request to " + friend.getName());
        friend.sendMessage("§aYou've gotten a friend request from " + player.getName());

        return false;
    }
}
