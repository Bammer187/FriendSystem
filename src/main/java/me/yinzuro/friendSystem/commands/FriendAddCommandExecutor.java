package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import static net.kyori.adventure.text.Component.text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FriendAddCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend add <Player>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou can't add yourself.");
            return true;
        }

        try {
            if (!canSendRequest(player, friend)) {
                player.sendMessage("§cYou already sent a friend request to this player or are already friends with them.");
                return false;
            } else if (!canSendRequest(friend, player)) {
                player.sendMessage("§cYou already have a friend request from this player.");
                return false;
            }
        } catch (SQLException e) {
            player.sendMessage("§cDatabase error while checking existing friend requests.");
            plugin.getLogger().severe("MySQL-ERROR while checking for existing friend request: " + e.getMessage());
            return false;
        }


        try {
            insertRequestIntoDatabase(player, friend);
        } catch (SQLException e) {
            return true;
        }

        player.sendMessage("§aSend friend request to " + friend.getName());

        Component message = text("§aYou've gotten a friend request from " + player.getName() + " ")
                .append(
                        text("§e[Click here to accept]")
                                .clickEvent(ClickEvent.runCommand("/friend accept " + player.getName()))
                                .hoverEvent(HoverEvent.showText(text("Click to accept the friend request")))
                ).append(
                        text(" ")
                .append(
                        text("§c[Click here to deny]")
                                .clickEvent(ClickEvent.runCommand("/friend deny " + player.getName()))
                                .hoverEvent(HoverEvent.showText(text("Click to deny the friend request")))
                ));

        friend.sendMessage(message);

        return false;
    }

    private void insertRequestIntoDatabase (Player fromPlayer, Player toPlayer) throws SQLException {
        try (PreparedStatement statement = FriendSystem.getDatabase().getConnection().prepareStatement("""
                INSERT INTO open_friend_requests (player_uuid, from_player_uuid) VALUES (?, ?);""")) {

            UUID playerUUID = fromPlayer.getUniqueId();
            UUID friendUUID = toPlayer.getUniqueId();

            statement.setString(1, friendUUID.toString());
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();

            FriendSystem.getDatabase().disconnect();
        } catch (SQLException e) {
            fromPlayer.sendMessage("§cThere was an error while saving to database");
            plugin.getLogger().severe("MySQL-ERROR while inserting into open_friend_requests" + e.getMessage());
        }
    }

    private boolean canSendRequest(Player fromPlayer, Player toPlayer) throws SQLException {
        UUID fromUUID = fromPlayer.getUniqueId();
        UUID toUUID = toPlayer.getUniqueId();

        String query = """
        SELECT 1 FROM open_friend_requests
        WHERE player_uuid = ? AND from_player_uuid = ?
        LIMIT 1;
        """;

        String checkFriendshipQuery = """
        SELECT 1 FROM friends
        WHERE (player_uuid = ? AND friend_uuid = ?)
           OR (player_uuid = ? AND friend_uuid = ?)
        LIMIT 1;
        """;

        try (Connection conn = FriendSystem.getDatabase().getConnection()) {
            // Check for existing open friend request
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, toUUID.toString());
                ps.setString(2, fromUUID.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(checkFriendshipQuery)) {
                ps.setString(1, fromUUID.toString());
                ps.setString(2, toUUID.toString());
                ps.setString(3, toUUID.toString());
                ps.setString(4, fromUUID.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
