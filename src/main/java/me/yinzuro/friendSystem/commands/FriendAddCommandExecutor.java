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
            commandSender.sendMessage(PREFIX + "§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage(PREFIX + "Usage: /friend add <Player>");
            return true;
        }

        String targetName = strings[0];

        Player targetOnline = Bukkit.getPlayerExact(targetName);
        OfflinePlayer target = targetOnline != null ? targetOnline : Bukkit.getOfflinePlayer(targetName);

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(PREFIX + "You can't add yourself.");
            return true;
        }

        if (target.getName() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(PREFIX + "This player was never online before.");
            return true;
        }

        try {
            if (canNotSendRequest(player, target)) {
                player.sendMessage(PREFIX + "You already sent a friend request to this player or are already friends with them.");
                return true;
            } else if (canNotSendRequest(target, player)) {
                player.sendMessage(PREFIX + "You already have a friend request from this player.");
                return true;
            }
        } catch (SQLException e) {
            player.sendMessage(PREFIX + "§cDatabase error while checking existing friend requests.");
            plugin.getLogger().severe("MySQL-ERROR while checking for existing friend request: " + e.getMessage());
            return true;
        }


        try {
            insertRequestIntoDatabase(player, target);
        } catch (SQLException e) {
            return true;
        }

        player.sendMessage(PREFIX + "Send friend request to §e" + target.getName() + "§7.");

        if (targetOnline != null) {
            sendMessageToFriend(player, targetOnline);
        }

        return true;
    }

    private void insertRequestIntoDatabase (Player fromPlayer, OfflinePlayer toPlayer) throws SQLException {
        try (PreparedStatement statement = FriendSystem.getDatabase().getConnection().prepareStatement("""
                INSERT INTO open_friend_requests (player_uuid, from_player_uuid) VALUES (?, ?);""")) {

            UUID playerUUID = fromPlayer.getUniqueId();
            UUID friendUUID = toPlayer.getUniqueId();

            statement.setString(1, friendUUID.toString());
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();

            FriendSystem.getDatabase().disconnect();
        } catch (SQLException e) {
            fromPlayer.sendMessage(PREFIX + "§cThere was an error while saving to database");
            plugin.getLogger().severe("MySQL-ERROR while inserting into open_friend_requests" + e.getMessage());
        }
    }

    private boolean canNotSendRequest(OfflinePlayer fromPlayer, OfflinePlayer toPlayer) throws SQLException {
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
                        return true;
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
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private void sendMessageToFriend(Player fromPlayer, Player toPlayer) {
        Component message = text(PREFIX + "§e" + fromPlayer.getName() + " §7wants to be your friend. ")
                .append(
                        text("§a[ACCEPT]")
                                .clickEvent(ClickEvent.runCommand("/friend accept " + fromPlayer.getName()))
                                .hoverEvent(HoverEvent.showText(text("Click to accept the friend request")))
                ).append(
                        text(" ")
                                .append(
                                        text("§c[DENY]")
                                                .clickEvent(ClickEvent.runCommand("/friend deny " + fromPlayer.getName()))
                                                .hoverEvent(HoverEvent.showText(text("Click to deny the friend request")))
                                ));

        toPlayer.sendMessage(message);
    }
}
