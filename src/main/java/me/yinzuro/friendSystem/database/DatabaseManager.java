package me.yinzuro.friendSystem.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
}
