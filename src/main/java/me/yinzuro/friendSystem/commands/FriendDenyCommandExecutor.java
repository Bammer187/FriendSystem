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

public class FriendDenyCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend deny <Player>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou can't deny yourself.");
            return true;
        }

        try {
            denyRequest(friend, player);
            player.sendMessage("§cYou denied the friend request from " + friend.getName());
            friend.sendMessage("§c" + player.getName() + " has denied your friend request.");
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while denying the request.");
            plugin.getLogger().severe("MySQL-ERROR while trying to delete from open_friend_requests: " + e.getMessage());
        }

        return false;
    }

    private void denyRequest(Player fromPlayer, Player toPlayer) throws SQLException {
        try (PreparedStatement statement = FriendSystem.getDatabase().getConnection().prepareStatement("""
                DELETE FROM open_friend_requests WHERE player_uuid = ? AND from_player_uuid = ?;""")) {

            UUID playerUUID = fromPlayer.getUniqueId();
            UUID friendUUID = toPlayer.getUniqueId();

            statement.setString(1, friendUUID.toString());
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();

            FriendSystem.getDatabase().disconnect();
        }
    }

    private boolean checkIfThereIsRequest(Player fromPlayer, Player toPlayer) throws SQLException {
        String query = """
        SELECT 1 FROM open_friend_requests
        WHERE player_uuid = ? AND from_player_uuid = ?
        LIMIT 1;
        """;

        try (Connection conn = FriendSystem.getDatabase().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, toPlayer.getUniqueId().toString());
            ps.setString(2, fromPlayer.getUniqueId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
