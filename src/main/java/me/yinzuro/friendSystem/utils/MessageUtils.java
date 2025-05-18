package me.yinzuro.friendSystem.utils;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageUtils {

    public static void sendPrivateMessage(Player fromPlayer, Player toPlayer, String message) throws SQLException {
        if (fromPlayer.equals(toPlayer)) {
            fromPlayer.sendMessage("§cYou can't message yourself.");
            return;
        }

        if (!checkIfPlayersAreFriends(fromPlayer, toPlayer)) {
            fromPlayer.sendMessage("§cYou aren't friends with " + toPlayer.getName());
            return;
        }

        fromPlayer.sendMessage("§7You §3» §7" + toPlayer.getName() + ": " + message);
        toPlayer.sendMessage("§7" + fromPlayer.getName() + " §3» §7You: " + message);
        saveToDatabaseLastMessage(fromPlayer, toPlayer);
    }

    private static boolean checkIfPlayersAreFriends(Player player1, Player player2) throws SQLException {
        String query = "SELECT 1 FROM friends WHERE player_uuid = ? AND friend_uuid = ? LIMIT 1;";
        try (Connection conn = FriendSystem.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, player1.getUniqueId().toString());
            ps.setString(2, player2.getUniqueId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void saveToDatabaseLastMessage(Player player1, Player player2) throws SQLException {
        String query = "UPDATE last_message SET friend_uuid = ? WHERE player_uuid = ?;";

        try (Connection conn = FriendSystem.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, player1.getUniqueId().toString());
            ps.setString(2, player2.getUniqueId().toString());
            ps.executeUpdate();

            ps.setString(1, player2.getUniqueId().toString());
            ps.setString(2, player1.getUniqueId().toString());
            ps.executeUpdate();
        }
    }
}
