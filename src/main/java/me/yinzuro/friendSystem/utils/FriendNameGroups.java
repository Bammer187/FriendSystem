package me.yinzuro.friendSystem.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record FriendNameGroups(List<String> onlineFriends, List<String> offlineFriends) {

    public FriendNameGroups(List<String> onlineFriends, List<String> offlineFriends) {
        this.onlineFriends = onlineFriends != null ? onlineFriends : new ArrayList<>();
        this.offlineFriends = offlineFriends != null ? offlineFriends : new ArrayList<>();
    }

    @Override
    public List<String> onlineFriends() {
        return Collections.unmodifiableList(onlineFriends);
    }

    @Override
    public List<String> offlineFriends() {
        return Collections.unmodifiableList(offlineFriends);
    }

    public List<String> getAllFriends() {
        List<String> allFriends = new ArrayList<>(onlineFriends);
        allFriends.addAll(offlineFriends);
        return allFriends;
    }
}
