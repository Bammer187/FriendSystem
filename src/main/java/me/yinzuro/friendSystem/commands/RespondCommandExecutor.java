package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RespondCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player.");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /respond <message>");
            return true;
        }

        try {
            UUID lastMessaged = getLastFriendMessagedUUID(player);
            if (lastMessaged != null) {
                Player friend = Bukkit.getPlayer(lastMessaged);

                if (friend == null) {
                    player.sendMessage("§cThe player is not online.");
                    return true;
                }
            } else {
                player.sendMessage("You didn't write with a player.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private UUID getLastFriendMessagedUUID(Player player) throws SQLException {
        String query = """
        SELECT friend_uuid FROM last_message
        WHERE player_uuid = ?
        LIMIT 1;
        """;

        try (Connection connection = FriendSystem.getDatabase().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String friendUUID = rs.getString("friend_uuid");
                    return friendUUID != null ? UUID.fromString(friendUUID) : null;
                } else {
                    return null;
                }
            }
        }
    }
}
