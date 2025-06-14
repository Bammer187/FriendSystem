package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
import me.yinzuro.friendSystem.utils.FriendListUtils;

import me.yinzuro.friendSystem.utils.FriendNameGroups;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FriendListCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        String friendListPage = "";

        if (strings.length == 0) {
            friendListPage = "1";
        }
        else if (strings.length == 1) {
            friendListPage = strings[0];
        }
        else {
            player.sendMessage("§cUsage /friend list <page>");
            return true;
        }

        int page;
        try {
            page = Integer.parseInt(friendListPage);
            if (page < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid page number.");
            return true;
        }

        FriendNameGroups group = FriendListUtils.getFriendNameGroups(player);
        List<String> allFriendNames = group.getAllFriends();
        List<String> onlineFriends = group.onlineFriends();

        double MAX_PLAYERS_PER_PAGE = 5.0;
        int totalPages = (int) Math.ceil((double) allFriendNames.size() / MAX_PLAYERS_PER_PAGE);
        if (page > totalPages) {
            player.sendMessage("§cPage does not exist. Max: " + totalPages);
            return true;
        }

        player.sendMessage("§6Your Friends §8(Page " + page + "/" + totalPages + "):");

        int start = (page - 1) * 5;
        int end = Math.min(start + 5, allFriendNames.size());
        String onlineStatus = "";

        for (int i = start; i < end; i++) {
            if(onlineFriends.contains(allFriendNames.get(i))) {
                onlineStatus = "§aONLINE";
            } else {
                onlineStatus = "§cOFFLINE";
            }
            player.sendMessage("§7" + allFriendNames.get(i) + " §3» " + onlineStatus);
        }

        return true;
    }
}
