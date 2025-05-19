package me.yinzuro.friendSystem.utils;

import java.util.ArrayList;
import java.util.List;

public class FriendNameGroups {

    private final List<String> onlineFriends;
    private final List<String> offlineFriends;

    public FriendNameGroups(List<String> onlineFriends, List<String> offlineFriends) {
        this.onlineFriends = onlineFriends != null ? onlineFriends : new ArrayList<>();
        this.offlineFriends = offlineFriends != null ? offlineFriends : new ArrayList<>();
    }
}
