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
            int page;
            try {
                page = Integer.parseInt(friendListPage);
                if (page < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid page number.");
                return true;
            }

            List<UUID> friends = getAllFriends(player);
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

            double MAX_PLAYERS_PER_PAGE = 5.0;
            int totalPages = (int) Math.ceil((double) allFriendNames.size() / MAX_PLAYERS_PER_PAGE);
            if (page > totalPages) {
                player.sendMessage("§cPage does not exist. Max: " + totalPages);
                return true;
            }

            player.sendMessage("§6Your Friends §8(Page " + page + "/" + totalPages + "):");

            int start = (page - 1) * 5;
            int end = Math.min(start + 5, allFriendNames.size());
            String onlineStatus = "";

            for (int i = start; i < end; i++) {
                if(onlineFriends.contains(allFriendNames.get(i))) {
                    onlineStatus = "§aONLINE";
                } else {
                    onlineStatus = "§cOFFLINE";
                }
                player.sendMessage("§7" + allFriendNames.get(i) + "§3» " + onlineStatus);
            }

        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while displaying your friend list.");
            plugin.getLogger().severe("MySQL-ERROR while reading from friends: " + e.getMessage());
        }

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
