package me.yinzuro.friendSystem.listener;

import static me.yinzuro.friendSystem.utils.ChatPrefix.PREFIX;
import static me.yinzuro.friendSystem.utils.FriendInventoryUtils.FRIEND_LIST;
import me.yinzuro.friendSystem.FriendSystem;
import me.yinzuro.friendSystem.utils.FriendInventoryUtils;
import me.yinzuro.friendSystem.utils.FriendListUtils;
import me.yinzuro.friendSystem.utils.FriendNameGroups;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class FriendListClickListener implements Listener {

    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, Integer> playerRequestsPages = new HashMap<>();
    private final JavaPlugin plugin = FriendSystem.getInstance();

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
        if (!FriendInventoryUtils.isHandledTitle(event.getView().title())) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        Component title = event.getView().title();
        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();

        if (title.equals(FriendInventoryUtils.FRIEND_LIST)) {
            handleFriendListNavigation(player, slot);
            return;
        }

        if (title.equals(FriendInventoryUtils.FRIEND_REQUESTS)) {
            handleFriendRequestsNavigation(player, slot);
            return;
        }

        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case CHEST -> {
                player.closeInventory();
                FriendInventoryUtils.runLater(() -> openFriendRequestsInventory(player, 1));
            }
            case BARRIER -> {
                player.closeInventory();
                if (title.equals(FriendInventoryUtils.FRIEND_REQUESTS) || title.equals(FriendInventoryUtils.REMOVE))
                    FriendInventoryUtils.runLater(() -> openFriendInventory(player, 1));
                else if (title.equals(FriendInventoryUtils.ACCEPT_DENY))
                    FriendInventoryUtils.runLater(() -> openFriendRequestsInventory(player, 1));
            }
            case FILLED_MAP -> {
                if (title.equals(FriendInventoryUtils.FRIEND_REQUESTS)) {
                    String name = FriendInventoryUtils.extractName(clicked.displayName());
                    player.closeInventory();
                    FriendInventoryUtils.runLater(() -> openAcceptDenyInventory(player, name));
                }
            }
            case PLAYER_HEAD, SKELETON_SKULL -> {

            }
            case LIME_DYE -> {

            }
            case RED_DYE -> {

            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Component titleFriendList = Component.text("§bYour friends");
        Component titleFriendRequests = Component.text("§bYour friend requests");
        if (event.getView().title().equals(titleFriendList) || event.getView().title().equals(titleFriendRequests)) {
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
        prevMeta.displayName(Component.text("§7← Previous Page"));
        previousPage.setItemMeta(prevMeta);
        friendsInventory.setItem(45, previousPage);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("§7Next Page →"));
        nextPage.setItemMeta(nextMeta);
        friendsInventory.setItem(53, nextPage);

        ItemStack friendRequests = new ItemStack(Material.CHEST);
        ItemMeta friendRequestsMeta = friendRequests.getItemMeta();
        friendRequestsMeta.displayName(Component.text("§eFriend requests"));
        friendRequests.setItemMeta(friendRequestsMeta);
        friendsInventory.setItem(49, friendRequests);

        player.openInventory(friendsInventory);
    }

    public void openFriendRequestsInventory(Player player, int page) {
        Component title = Component.text("§bYour friend requests");
        Inventory friendRequestsInventory = Bukkit.createInventory(null, 54, title);

        for (int i=0; i<9; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            friendRequestsInventory.setItem(i, glassPane);
        }

        for (int i=46; i<53; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            friendRequestsInventory.setItem(i, glassPane);
        }

        List<UUID> openFriendRequests = getOpenFriendRequests(player);
        List<String> friendNames = new ArrayList<>();
        for (UUID uuid : openFriendRequests) {
            String friendName = Bukkit.getOfflinePlayer(uuid).getName();
            friendNames.add(friendName);
        }

        int start = (page - 1) * 36;
        int end = Math.min(start + 36, friendNames.size());

        for (int i = start; i < end; i++) {
            String friendName = friendNames.get(i);
            ItemStack requestPaper = new ItemStack(Material.FILLED_MAP);

            ItemMeta requestPaperMeta = requestPaper.getItemMeta();
            requestPaperMeta.displayName(Component.text("§e" + friendName));
            requestPaper.setItemMeta(requestPaperMeta);

            friendRequestsInventory.setItem(i + 9, requestPaper);
        }

        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = previousPage.getItemMeta();
        prevMeta.displayName(Component.text("§7← Previous Page"));
        previousPage.setItemMeta(prevMeta);
        friendRequestsInventory.setItem(45, previousPage);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("§7Next Page →"));
        nextPage.setItemMeta(nextMeta);
        friendRequestsInventory.setItem(53, nextPage);

        ItemStack backToFriendList = new ItemStack(Material.BARRIER);
        ItemMeta backToFriendListMeta = backToFriendList.getItemMeta();
        backToFriendListMeta.displayName(Component.text("§cBack"));
        backToFriendList.setItemMeta(backToFriendListMeta);
        friendRequestsInventory.setItem(49, backToFriendList);

        player.openInventory(friendRequestsInventory);
    }

    public void openAcceptDenyInventory(Player player, String friendName) {
        Component title = Component.text("§bFriend request");
        Inventory acceptDenyInventory = Bukkit.createInventory(null, 54, title);

        for (int i=0; i<9; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            acceptDenyInventory.setItem(i, glassPane);
        }

        for (int i=45; i<54; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            acceptDenyInventory.setItem(i, glassPane);
        }

        ItemStack friendNamePaper = new ItemStack(Material.FILLED_MAP);
        ItemMeta friendNameMeta = friendNamePaper.getItemMeta();
        friendNameMeta.displayName(Component.text(friendName));
        friendNamePaper.setItemMeta(friendNameMeta);
        acceptDenyInventory.setItem(22, friendNamePaper);

        ItemStack accept = new ItemStack(Material.LIME_DYE);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.displayName(Component.text("§aAccept friend request"));
        accept.setItemMeta(acceptMeta);
        acceptDenyInventory.setItem(30, accept);

        ItemStack deny = new ItemStack(Material.RED_DYE);
        ItemMeta denyMeta = deny.getItemMeta();
        denyMeta.displayName(Component.text("§4Deny friend request"));
        deny.setItemMeta(denyMeta);
        acceptDenyInventory.setItem(32, deny);

        ItemStack backToFriendRequests = new ItemStack(Material.BARRIER);
        ItemMeta backToFriendRequestsMeta = backToFriendRequests.getItemMeta();
        backToFriendRequestsMeta.displayName(Component.text("§cBack"));
        backToFriendRequests.setItemMeta(backToFriendRequestsMeta);
        acceptDenyInventory.setItem(49, backToFriendRequests);

        player.openInventory(acceptDenyInventory);
    }

    public void openRemoveInventory(Player player, String friendName) {
        Component title = Component.text("§bRemove friend");
        Inventory removeInventory = Bukkit.createInventory(null, 54, title);

        for (int i=0; i<9; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            removeInventory.setItem(i, glassPane);
        }

        for (int i=45; i<54; i++) {
            ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            removeInventory.setItem(i, glassPane);
        }

        ItemStack friendNameMap = new ItemStack(Material.FILLED_MAP);
        ItemMeta friendNameMapMeta = friendNameMap.getItemMeta();
        friendNameMapMeta.displayName(Component.text("§e" + friendName));
        friendNameMap.setItemMeta(friendNameMapMeta);
        removeInventory.setItem(22, friendNameMap);

        ItemStack remove = new ItemStack(Material.RED_DYE);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.displayName(Component.text("§4Remove friend"));
        remove.setItemMeta(removeMeta);
        removeInventory.setItem(31, remove);

        ItemStack backToFriendList = new ItemStack(Material.BARRIER);
        ItemMeta backToFriendListMeta = backToFriendList.getItemMeta();
        backToFriendListMeta.displayName(Component.text("§cBack"));
        backToFriendList.setItemMeta(backToFriendListMeta);
        removeInventory.setItem(49, backToFriendList);

        player.openInventory(removeInventory);
    }

    private List<UUID> getOpenFriendRequests(Player player) {
        String query = """
        SELECT from_player_uuid FROM open_friend_requests
        WHERE player_uuid = ?;
        """;

        List<UUID> friendUUIDs = new ArrayList<>();

        try (Connection connection = FriendSystem.getDatabase().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String uuidStr = rs.getString("from_player_uuid");
                    if (uuidStr != null) {
                        friendUUIDs.add(UUID.fromString(uuidStr));
                    }
                }
            }
        } catch (Exception e) {
            player.sendMessage(PREFIX + "§cThere was an error while getting your open requests.");
            plugin.getLogger().severe("MySQL-ERROR while reading from open_friend_requests.");
        }

        return friendUUIDs;
    }

    private String removeFirstAndLastChar(String str) {
        str = str.substring(1, str.length() - 1);

        return str;
    }

    private void handleFriendListNavigation(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        if (slot == 45) {
            int page = Math.max(1, playerPages.get(uuid) - 1);
            playerPages.put(uuid, page);
            openFriendInventory(player, page);
        } else if (slot == 53) {
            double MAX_PLAYER_PER_PAGE = 36.0;
            List<String> allNames = FriendListUtils.getFriendNameGroups(player).getAllFriends();
            int maxPages = (int) Math.ceil(allNames.size() / MAX_PLAYER_PER_PAGE);
            int page = Math.min(maxPages, playerPages.get(uuid) + 1);
            playerPages.put(uuid, page);
            openFriendInventory(player, page);
        }
    }

    private void handleFriendRequestsNavigation(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        if (slot == 45) {
            int page = Math.max(1, playerRequestsPages.get(uuid) - 1);
            playerRequestsPages.put(uuid, page);
            openFriendRequestsInventory(player, page);
        } else if (slot == 53) {
            double MAX = 36.0;
            List<UUID> uuids = getOpenFriendRequests(player);
            int maxPages = (int) Math.ceil(uuids.size() / MAX);
            int page = Math.min(maxPages, playerRequestsPages.get(uuid) + 1);
            playerRequestsPages.put(uuid, page);
            openFriendRequestsInventory(player, page);
        }
    }
}
