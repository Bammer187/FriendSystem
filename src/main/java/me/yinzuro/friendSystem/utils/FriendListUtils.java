package me.yinzuro.friendSystem.utils;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FriendListUtils {

    private static final JavaPlugin plugin = FriendSystem.getInstance();

    public static List<UUID> getAllFriends(Player player) throws SQLException {
        String query = """
        SELECT friend_uuid FROM friends
        WHERE player_uuid = ?;
        """;

        List<UUID> friendUUIDs = new ArrayList<>();

        try (Connection connection = FriendSystem.getDatabase().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String uuidStr = rs.getString("friend_uuid");
                    if (uuidStr != null) {
                        friendUUIDs.add(UUID.fromString(uuidStr));
                    }
                }
            }
        }

        return friendUUIDs;
    }

    public static List<String> getSortedFriendNames(Player player) {
        List<UUID> friends;

        try {
            friends = FriendListUtils.getAllFriends(player);
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while getting all your friends");
            plugin.getLogger().severe("MySQL-ERROR while reading from friends: " + e.getMessage());
            return Collections.emptyList();
        }
        List<String> onlineFriends = new ArrayList<>();
        List<String> offlineFriends = new ArrayList<>();

        for (UUID friendUUID : friends) {
            Player friend = Bukkit.getPlayer(friendUUID);
            if (friend != null && friend.isOnline()) {
                onlineFriends.add(friend.getName());
            } else {
                offlineFriends.add(Bukkit.getOfflinePlayer(friendUUID).getName());
            }
        }

        List<String> allFriendNames = new ArrayList<>();
        allFriendNames.addAll(onlineFriends);
        allFriendNames.addAll(offlineFriends);

        return allFriendNames;
    }
}
