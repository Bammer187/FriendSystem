package me.yinzuro.friendSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FriendAddCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return false;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend add <Player>");
            return false;
        }

        UUID playerUUID = player.getUniqueId();
        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend== null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return false;
        }
        UUID friendUUID = friend.getUniqueId();

        player.sendMessage("§aSend friend request to " + friend.getName());

        return false;
    }
}
