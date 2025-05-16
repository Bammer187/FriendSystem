package me.yinzuro.friendSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import static net.kyori.adventure.text.Component.text;

public class FriendAddCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou aren't a player");
            return true;
        }

        if (strings.length != 1) {
            player.sendMessage("§cUsage: /friend add <Player>");
            return true;
        }

        Player friend = Bukkit.getPlayerExact(strings[0]);
        if (friend == null) {
            player.sendMessage("§cCouldn't find a player with the given name.");
            return true;
        } else if (friend == player) {
            player.sendMessage("§cYou can't add yourself.");
            return true;
        }

        player.sendMessage("§aSend friend request to " + friend.getName());

        Component message = text("§aYou've gotten a friend request from " + player.getName() + " ")
                .append(
                        text("§e[Click here to accept]")
                                .clickEvent(ClickEvent.runCommand("/friend accept " + player.getName()))
                                .hoverEvent(HoverEvent.showText(text("Click to accept the friend request")))
                );

        friend.sendMessage(message);

        return false;
    }
}
