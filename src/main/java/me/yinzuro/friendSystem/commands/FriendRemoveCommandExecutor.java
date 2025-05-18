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
import java.util.UUID;

public class FriendRemoveCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player.");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend remove <Player>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou don't have yourself as friend.");
            return true;
        }

        try {
            if(canRemoveFriend(player, friend)) {
                try {
                    removeFriend(player, friend);
                    player.sendMessage("§aYou've ended the friendship with " + friend.getName());
                    friend.sendMessage("§c" + player.getName() + " has ended the friendship with you.");
                } catch (SQLException e) {
                    player.sendMessage("§cThere was an error while deleting this friend.");
                    plugin.getLogger().severe("MySQL-ERROR while deleting from friends: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while deleting this friend.");
            plugin.getLogger().severe("MySQL-ERROR while selecting from friends: " + e.getMessage());
        }

        return false;
    }

    private boolean canRemoveFriend(Player player1, Player player2) throws SQLException {
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
                return rs.next();
            }
        }
    }

    private void removeFriend(Player player1, Player player2) throws SQLException {
        try (PreparedStatement statement = FriendSystem.getDatabase().getConnection().prepareStatement("""
                DELETE FROM friends WHERE player_uuid = ? AND friend_uuid = ?;""")) {

            UUID playerUUID = player1.getUniqueId();
            UUID friendUUID = player2.getUniqueId();

            statement.setString(1, friendUUID.toString());
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();

            statement.setString(1, playerUUID.toString());
            statement.setString(2, friendUUID.toString());
            statement.executeUpdate();

            FriendSystem.getDatabase().disconnect();
        }
    }
}
