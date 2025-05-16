package me.yinzuro.friendSystem;

import me.yinzuro.friendSystem.commands.FriendCommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("friend").setExecutor(new FriendCommandExecutor());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
