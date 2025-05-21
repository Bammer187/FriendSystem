package me.yinzuro.friendSystem.utils;

import me.yinzuro.friendSystem.FriendSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendInventoryUtils {

    private static final JavaPlugin plugin = FriendSystem.getInstance();

    public static final Component FRIEND_LIST = Component.text("§bYour friends");
    public static final Component FRIEND_REQUESTS = Component.text("§bYour friend requests");
    public static final Component ACCEPT_DENY = Component.text("§bFriend request");
    public static final Component REMOVE = Component.text("§bRemove friend");

    public static boolean isHandledTitle(Component title) {
        return title.equals(FriendInventoryUtils.FRIEND_LIST) || title.equals(FriendInventoryUtils.FRIEND_REQUESTS)
                || title.equals(FriendInventoryUtils.ACCEPT_DENY) || title.equals(FriendInventoryUtils.REMOVE);
    }

    public static void runLater(Runnable task) {
        Bukkit.getScheduler().runTaskLater(plugin, task, 2L);
    }

    private static String extractName(Component displayName) {
        return PlainTextComponentSerializer.plainText().serialize(displayName).replace("§e", "").strip();
    }

    public static void handleCommandItem(Player player, Inventory inv, String baseCommand) {
        ItemStack nameItem = inv.getItem(22);
        if (nameItem != null && nameItem.hasItemMeta() && nameItem.getItemMeta().hasDisplayName()) {
            String name = extractName(nameItem.getItemMeta().displayName());
            player.performCommand(baseCommand + " " + name);
            player.closeInventory();
        }
    }
}
