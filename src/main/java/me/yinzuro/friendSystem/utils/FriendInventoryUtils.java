package me.yinzuro.friendSystem.utils;

import me.yinzuro.friendSystem.FriendSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendInventoryUtils {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    static final Component FRIEND_LIST = Component.text("§bYour friends");
    static final Component FRIEND_REQUESTS = Component.text("§bYour friend requests");
    static final Component ACCEPT_DENY = Component.text("§bFriend request");
    static final Component REMOVE = Component.text("§bRemove friend");

    public static boolean isHandledTitle(Component title) {
        return title.equals(FriendInventoryUtils.FRIEND_LIST) || title.equals(FriendInventoryUtils.FRIEND_REQUESTS)
                || title.equals(FriendInventoryUtils.ACCEPT_DENY) || title.equals(FriendInventoryUtils.REMOVE);
    }

    private void runLater(Runnable task) {
        Bukkit.getScheduler().runTaskLater(plugin, task, 2L);
    }

    private String extractName(Component displayName) {
        return PlainTextComponentSerializer.plainText().serialize(displayName).replace("§e", "").strip();
    }
}
