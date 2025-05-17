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

import java.sql.PreparedStatement;
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

        player.sendMessage("§aSend friend request to " + friend.getName());

        Component message = text("§aYou've gotten a friend request from " + player.getName() + " ")
                .append(
                        text("§e[Click here to accept]")
                                .clickEvent(ClickEvent.runCommand("/friend accept " + player.getName()))
                                .hoverEvent(HoverEvent.showText(text("Click to accept the friend request")))
                );

        friend.sendMessage(message);

        return false;
    }

    private void insertRequestIntoDatabase (Player fromPlayer, Player toPlayer) throws SQLException {
        try (PreparedStatement statement = FriendSystem.getDatabase().getConnection().prepareStatement("""
                INSERT INTO open_friend_requests (player_uuid, friend_uuid) VALUES (?, ?)""")) {

            UUID playerUUID = fromPlayer.getUniqueId();
            UUID friendUUID = toPlayer.getUniqueId();

            statement.setString(1, playerUUID.toString());
            statement.setString(2, friendUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            fromPlayer.sendMessage("§cThere was an error while saving to database");
            plugin.getLogger().severe("MySQL-ERROR while inserting into open_friend_requests" + e.getMessage());
        }
    }
}
