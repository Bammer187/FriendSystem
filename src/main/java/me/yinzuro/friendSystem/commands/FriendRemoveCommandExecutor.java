package me.yinzuro.friendSystem.commands;

import static me.yinzuro.friendSystem.utils.ChatPrefix.PREFIX;
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
import java.util.UUID;

public class FriendRemoveCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(PREFIX + "§cYou aren't a player.");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage(PREFIX + "Usage: /friend remove <Player>");
            return true;
        }

        String targetName = strings[0];

        Player targetOnline = Bukkit.getPlayerExact(targetName);
        OfflinePlayer target = targetOnline != null ? targetOnline : Bukkit.getOfflinePlayer(targetName);

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(PREFIX + "You can't remove yourself.");
            return true;
        }

        if (target.getName() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(PREFIX + "This player was never online before.");
            return true;
        }

        try {
            if(canRemoveFriend(player, target)) {
                removeFriend(player, target);
            } else {
                player.sendMessage(PREFIX + "You aren't friends with this player.");
            }
        } catch (SQLException e) {
            player.sendMessage(PREFIX + "§cThere was an error while deleting this friend.");
            plugin.getLogger().severe("MySQL-ERROR while selecting from friends: " + e.getMessage());
        }

        return true;
    }

    private boolean canRemoveFriend(Player player1, OfflinePlayer player2) throws SQLException {
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

    private void removeFriendSQL(Player player1, OfflinePlayer player2) throws SQLException {
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

    private void removeFriend(Player player, OfflinePlayer target) throws SQLException {
        removeFriendSQL(player, target);
        player.sendMessage(PREFIX + "§7You've ended the friendship with §e" + target.getName() + "§7.");

        if (target.isOnline() && target instanceof Player targetOnline) {
            targetOnline.sendMessage(PREFIX + "§e" + player.getName() + " §7has ended the friendship with you.");
        }
    }
}
