package me.yinzuro.friendSystem.listener;

import me.yinzuro.friendSystem.utils.FriendListUtils;
import me.yinzuro.friendSystem.utils.FriendNameGroups;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

            FriendNameGroups group = FriendListUtils.getFriendNameGroups(player);
            List<String> allFriendNames = group.getAllFriends();
            List<String> onlineFriends = group.onlineFriends();

            double MAX_PLAYERS_PER_PAGE = 36.0;
            int totalPages = (int) Math.ceil((double) allFriendNames.size() / MAX_PLAYERS_PER_PAGE);
            int page = 1;

            int start = (page - 1) * 36;
            int end = Math.min(start + 36, allFriendNames.size());

            for (int i = start; i < end; i++) {
                String friendName = allFriendNames.get(i);
                if(onlineFriends.contains(friendName)) {
                    ItemStack friendHeadOnline = getFriendHeadOnline(allFriendNames, i);
                    friendsInventory.setItem(i + 9, friendHeadOnline);

                } else {
                    ItemStack friendHeadOffline = getFriendHeadOffline(allFriendNames, i);
                    friendsInventory.setItem(i + 9, friendHeadOffline);
                }
            }

            player.openInventory(friendsInventory);
        }
    }

    private static @NotNull ItemStack getFriendHeadOnline(List<String> allFriendNames, int i) {
        ItemStack friendHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) friendHead.getItemMeta();

        Player friend = Bukkit.getPlayer(allFriendNames.get(i));
        meta.setOwningPlayer(friend);

        Component friendNameComponent = Component.text(allFriendNames.get(i)).decoration(TextDecoration.ITALIC, false);;
        meta.displayName(friendNameComponent);

        List<Component> lore = List.of(
                Component.text("§7Status: ").append(Component.text("§aONLINE"))
        );
        meta.lore(lore);

        friendHead.setItemMeta(meta);
        return friendHead;
    }

    private static @NotNull ItemStack getFriendHeadOffline(List<String> allFriendNames, int i) {
        ItemStack friendHeadOffline = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta meta = friendHeadOffline.getItemMeta();

        Component friendNameComponent = Component.text(allFriendNames.get(i)).decoration(TextDecoration.ITALIC, false);
        ;
        meta.displayName(friendNameComponent);

        List<Component> lore = List.of(
                Component.text("§7Status: ").append(Component.text("§cOFFLINE"))
        );
        meta.lore(lore);

        friendHeadOffline.setItemMeta(meta);
        return friendHeadOffline;
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
}
