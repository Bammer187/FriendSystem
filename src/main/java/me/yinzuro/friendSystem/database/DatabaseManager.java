package me.yinzuro.friendSystem.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    private final String host, database, username, password;
    private final int port;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        host = config.getString("mysql.host");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
        port = config.getInt("mysql.port");
    }

    public void connect() throws SQLException {

        connection = getConnection();
        createTables();
        disconnect();
    }

    public void disconnect() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        return DriverManager.getConnection(url, username, password);
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
                """,
                """
                CREATE TABLE last_message (
                    player_uuid CHAR(36) PRIMARY KEY,
                    friend_uuid CHAR(36),
                    FOREIGN KEY (player_uuid) REFERENCES players(uuid),
                    FOREIGN KEY (friend_uuid) REFERENCES players(uuid)
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
