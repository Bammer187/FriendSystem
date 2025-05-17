package me.yinzuro.friendSystem.listener;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        try (Connection connection = FriendSystem.getDatabase().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT IGNORE INTO players (uuid) VALUES (?)")) {

            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            FriendSystem.getDatabase().disconnect();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert player into database: "  + e.getMessage());
        }
    }
}
