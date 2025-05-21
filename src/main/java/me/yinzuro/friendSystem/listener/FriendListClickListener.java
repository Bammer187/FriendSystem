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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FriendListClickListener implements Listener {

    private final Map<UUID, Integer> playerPages = new HashMap<>();

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            int page = playerPages.getOrDefault(player.getUniqueId(), 1);
            playerPages.put(player.getUniqueId(), 1);
            openFriendInventory(player, page);
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
        if (!event.getView().title().equals(title)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == 45) {
            int newPage = Math.max(1, playerPages.get(player.getUniqueId()) - 1);
            playerPages.put(player.getUniqueId(), newPage);
            openFriendInventory(player, newPage);
        }
        else if (slot == 53) {
            double MAX_PLAYERS_PER_PAGE = 36.0;
            FriendNameGroups group = FriendListUtils.getFriendNameGroups(player);
            List<String> allFriendNames = group.getAllFriends();

            int maxPages = (int) Math.ceil((double) allFriendNames.size() / MAX_PLAYERS_PER_PAGE);
            int newPage = Math.min(maxPages, playerPages.get(player.getUniqueId()) + 1);
            playerPages.put(player.getUniqueId(), newPage);
            openFriendInventory(player, newPage);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Component title = Component.text("§bYour friends");
        if (event.getView().title().equals(title)) {
            event.setCancelled(true);
        }
    }

    public void openFriendInventory(Player player, int page) {
        Component title = Component.text("§bYour friends");
        Inventory friendsInventory = Bukkit.createInventory(null, 54, title);

        for (int i=0; i<9; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            friendsInventory.setItem(i, glassPane);
        }

        for (int i=46; i<53; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            friendsInventory.setItem(i, glassPane);
        }

        FriendNameGroups group = FriendListUtils.getFriendNameGroups(player);
        List<String> allFriendNames = group.getAllFriends();
        List<String> onlineFriends = group.onlineFriends();

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

        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = previousPage.getItemMeta();
        prevMeta.displayName(Component.text("§7← Previous Page").decoration(TextDecoration.ITALIC, false));
        previousPage.setItemMeta(prevMeta);
        friendsInventory.setItem(45, previousPage);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("§7Next Page →").decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextMeta);
        friendsInventory.setItem(53, nextPage);

        ItemStack friendRequets = new ItemStack(Material.CHEST);
        ItemMeta friendRequestsMeta = friendRequets.getItemMeta();
        friendRequestsMeta.displayName(Component.text("§eFriend requests"));
        friendRequets.setItemMeta(friendRequestsMeta);
        friendsInventory.setItem(49, friendRequets);

        player.openInventory(friendsInventory);
    }

    public void openFriendRequestsInventory(Player player, int page) {
        Component title = Component.text("bYour friend requests");
        Inventory friendsInventory = Bukkit.createInventory(null, 54, title);

        for (int i=0; i<9; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            friendsInventory.setItem(i, glassPane);
        }

        for (int i=46; i<53; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            friendsInventory.setItem(i, glassPane);
        }
    }
}
