package me.yinzuro.friendSystem.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        createTables();
    }

    public void disconnect() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTables() throws SQLException {
        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS players (
                    uuid CHAR(36) PRIMARY KEY
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS friends (
                    player_uuid CHAR(36) NOT NULL,
                    friend_uuid CHAR(36) NOT NULL,
                    PRIMARY KEY (player_uuid, friend_uuid),
                    FOREIGN KEY (player_uuid) REFERENCES players(uuid),
                    FOREIGN KEY (friend_uuid) REFERENCES players(uuid)
                );
                """,
                """
                CREATE table IF NOT EXISTS open_friend_requests (
                    player_uuid CHAR(36) NOT NULL,
                    from_player_uuid CHAR(36) NOT NULL,
                    PRIMARY KEY (player_uuid, from_player_uuid),
                    FOREIGN KEY (player_uuid) REFERENCES players(uuid),
                    FOREIGN KEY (from_player_uuid) REFERENCES players(uuid)
                );
                """
        };

        for (String statement : statements){
            try (PreparedStatement ps = connection.prepareStatement(statement)) {
                ps.execute();
            } catch (SQLException e) {
                plugin.getLogger().severe("MySQL-ERROR while creating tables: " + e.getMessage());
            }
        }
    }
}
