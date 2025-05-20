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

public class FriendDenyCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(PREFIX + "§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage(PREFIX + "Usage: /friend deny <Player>");
            return true;
        }

        String targetName = strings[0];

        Player targetOnline = Bukkit.getPlayerExact(targetName);
        OfflinePlayer target = targetOnline != null ? targetOnline : Bukkit.getOfflinePlayer(targetName);

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(PREFIX + "You can't deny yourself.");
            return true;
        }

        if (target.getName() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(PREFIX + "This player was never online before.");
            return true;
        }

        try {
            if (checkIfThereIsRequest(target, player)) {
                try {
                    denyRequest(target, player);
                    player.sendMessage(PREFIX + "You denied the friend request from §e" + target.getName() + "§7.");
                    if (targetOnline != null) {
                        targetOnline.sendMessage(PREFIX + "§e" + player.getName() + " §7has denied your friend request.");
                    }
                } catch (SQLException e) {
                    player.sendMessage(PREFIX + "§cThere was an error while denying the request.");
                    plugin.getLogger().severe("MySQL-ERROR while trying to delete from open_friend_requests: " + e.getMessage());
                }
            } else {
                player.sendMessage(PREFIX + "You don't have an open request from this player.");
            }
        } catch (SQLException e) {
            player.sendMessage(PREFIX + "§cThere was an error while denying the request.");
            plugin.getLogger().severe("§cMySQL-ERROR while getting friend request: " + e.getMessage());
        }

        return true;
    }

    private void denyRequest(OfflinePlayer fromPlayer, Player toPlayer) throws SQLException {
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

    private boolean checkIfThereIsRequest(OfflinePlayer fromPlayer, Player toPlayer) throws SQLException {
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
