package me.yinzuro.friendSystem.commands;

import me.yinzuro.friendSystem.FriendSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class FriendDenyCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin = FriendSystem.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend deny <Player>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou can't deny yourself.");
            return true;
        }

        try {
            denyRequest(friend, player);
        } catch (SQLException e) {
            player.sendMessage("§cThere was an error while denying the request.");
            plugin.getLogger().severe("MySQL-ERROR while trying to delete from open_friend_requests: " + e.getMessage());
        }

        return false;
    }

    private void denyRequest(Player fromPlayer, Player toPlayer) throws SQLException {

    }
}
