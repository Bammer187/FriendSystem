package me.yinzuro.friendSystem.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port");
        String database = plugin.getConfig().getString("mysql.database");
        String username = plugin.getConfig().getString("mysql.username");
        String password = plugin.getConfig().getString("mysql.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        this.connection = DriverManager.getConnection(url, username, password);
    }
}
