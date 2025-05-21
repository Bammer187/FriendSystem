package me.yinzuro.friendSystem.utils;

import net.kyori.adventure.text.Component;

public final class FriendInventoryUtils {
    static final Component FRIEND_LIST = Component.text("§bYour friends");
    static final Component FRIEND_REQUESTS = Component.text("§bYour friend requests");
    static final Component ACCEPT_DENY = Component.text("§bFriend request");
    static final Component REMOVE = Component.text("§bRemove friend");

    public static boolean isHandledTitle(Component title) {
        return title.equals(FriendInventoryUtils.FRIEND_LIST) || title.equals(FriendInventoryUtils.FRIEND_REQUESTS)
                || title.equals(FriendInventoryUtils.ACCEPT_DENY) || title.equals(FriendInventoryUtils.REMOVE);
    }

    private void runLater(Runnable task) {

    }
}
