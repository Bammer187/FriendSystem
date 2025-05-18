package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
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
            if(checkIfPlayerAreFriends(player, friend)) {
                player.sendMessage("§7You " + "§3» " + "§7" + friend.getName() + ": " + strings[1]);
                friend.sendMessage("§7" + player.getName() + "§3 » " + "§7" + "You: " + strings[1]);
            } else {
                player.sendMessage("§cYou aren't friends with " + friend.getName());
            }
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while trying to send the message");
            plugin.getLogger().severe("MySQL-ERROR while reading from friends: " + e.getMessage());
        }

        return false;
    }

    private boolean checkIfPlayerAreFriends(Player player1, Player player2) throws SQLException {
        String query = """
        SELECT 1 FROM friends
        WHERE player_uuid = ? AND friend_uuid = ?
        LIMIT 1;
        """;

        try (Connection conn = FriendSystem.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, player1.getUniqueId().toString());
            ps.setString(2, player2.getUniqueId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                FriendSystem.getDatabase().disconnect();
                return rs.next();
            }
        }
    }
}
