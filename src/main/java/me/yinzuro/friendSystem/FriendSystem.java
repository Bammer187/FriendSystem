package me.yinzuro.friendSystem;

import me.yinzuro.friendSystem.database.DatabaseManager;
import me.yinzuro.friendSystem.commands.FriendCommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendSystem extends JavaPlugin {

    private static FriendSystem instance;
    private static DatabaseManager database;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        instance = this;
        database = new DatabaseManager(this);

        try {
            database.connect();
            getLogger().info("MySQL connection successfully established.");
        } catch (Exception e) {
            getLogger().severe("Error with the MySQL connection: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }

        getCommand("friend").setExecutor(new FriendCommandExecutor());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (database != null && database.isConnected()) {
            database.disconnect();
        }
    }

    public static DatabaseManager getDatabase() {
        return database;
    }

    public static FriendSystem getInstance() {
        return instance;
    }
}
