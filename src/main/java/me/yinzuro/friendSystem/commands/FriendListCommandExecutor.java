package me.yinzuro.friendSystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class FriendListCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        String friendListPage = "";

        if (strings.length == 0) {
            friendListPage = "1";
        }
        else if (strings.length == 1) {
            friendListPage = strings[0];
        }
        else {
            player.sendMessage("§cUsage /friend list <page>");
            return true;
        }
        player.sendMessage("§7[§4Friends]§7] List of your friends: §3Page " + friendListPage);

        return true;
    }

    private UUID[] getAllFriends(Player player) throws SQLException {

    }
}
