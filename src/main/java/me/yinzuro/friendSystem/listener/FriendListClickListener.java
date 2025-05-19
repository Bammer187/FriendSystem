package me.yinzuro.friendSystem.listener;

import me.yinzuro.friendSystem.FriendSystem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendListClickListener implements Listener {

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Component title = Component.text("§bYour friends");
            Inventory friendsInventory = Bukkit.createInventory(null, 54, title);

            for (int i=0; i<9; i++) {
                ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                friendsInventory.setItem(i, glassPane);
            }

            for (int i=45; i<54; i++) {
                ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                friendsInventory.setItem(i, glassPane);
            }

            player.openInventory(friendsInventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = Component.text("§bYour friends");
        if (event.getView().title().equals(title)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Component title = Component.text("§bYour friends");
        if (event.getView().title().equals(title)) {
            event.setCancelled(true);
        }
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
