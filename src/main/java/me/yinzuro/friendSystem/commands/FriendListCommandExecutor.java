package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendListCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

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
        try {
            List<UUID> friends = getAllFriends(player);
            List<Player> onlineFriends = new ArrayList<>();
            List<OfflinePlayer> offlineFriends = new ArrayList<>();

            for (UUID friendUUID : friends) {
                Player friend = Bukkit.getPlayer(friendUUID);
                if (friend != null && friend.isOnline()) {
                    onlineFriends.add(friend);
                } else {
                    offlineFriends.add(Bukkit.getOfflinePlayer(friendUUID));
                }
            }
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while displaying your friend list.");
            plugin.getLogger().severe("MySQL-ERROR while reading from friends: " + e.getMessage());
        }

        player.sendMessage("§7[§4Friends§7] List of your friends: §3Page " + friendListPage);

        return true;
    }

    private List<UUID> getAllFriends(Player player) throws SQLException {
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
}
