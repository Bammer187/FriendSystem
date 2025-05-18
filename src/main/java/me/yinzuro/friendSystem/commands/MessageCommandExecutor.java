package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
import me.yinzuro.friendSystem.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player.");
            return true;
        }

        if (strings.length != 2) {
            player.sendMessage("§cUsage: /message <Player> <message>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou can't message yourself.");
            return true;
        }

        try {
                MessageUtils.sendPrivateMessage(player, friend, strings[1]);
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while trying to send the message");
            plugin.getLogger().severe("MySQL-ERROR while reading from friends: " + e.getMessage());
        }

        return false;
    }
}
