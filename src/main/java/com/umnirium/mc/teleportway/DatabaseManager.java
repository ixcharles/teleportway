package com.umnirium.mc.teleportway;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.function.Consumer;

public class DatabaseManager {
    private final TeleportWay plugin;
    private Connection connection;
    private final ConfigManager config;

    public DatabaseManager(TeleportWay plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        initializeDatabase();
    }

    private void initializeDatabase() {
        File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String dbName = "teleportway.db";
        String url = "jdbc:sqlite:" + dataFolder + File.separator + dbName;

        try {
            connection = DriverManager.getConnection(url);
            createTable();
            plugin.getLogger().info("SQLite database connected!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS player_spawns (" +
                "uuid TEXT PRIMARY KEY, " +
                "username TEXT, " +
                "world TEXT NOT NULL, " +
                "x DOUBLE NOT NULL, " +
                "y DOUBLE NOT NULL, " +
                "z DOUBLE NOT NULL, " +
                "yaw FLOAT NOT NULL, " +
                "pitch FLOAT NOT NULL" +
                ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing SQLite connection: " + e.getMessage());
        }
    }

    public void savePlayerSpawnAsync(Player player, Location location) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT OR REPLACE INTO player_spawns (uuid, username, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setString(3, location.getWorld().getName());
                stmt.setDouble(4, location.getX());
                stmt.setDouble(5, location.getY());
                stmt.setDouble(6, location.getZ());
                stmt.setFloat(7, location.getYaw());
                stmt.setFloat(8, location.getPitch());
                stmt.executeUpdate();

                player.sendRichMessage(config.getMessage("setspawn-success"));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error saving spawn: " + e.getMessage());

                player.sendRichMessage(config.getMessage("setspawn-fail"));
            }
        });
    }

    public void getPlayerSpawnAsync(Player player, Consumer<Location> callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT world, x, y, z, yaw, pitch FROM player_spawns WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Location loc = new Location(
                            plugin.getServer().getWorld(rs.getString("world")),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );
                    plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(loc));
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(null));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error retrieving spawn: " + e.getMessage());
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(null));
            }
        });
    }

    public void deletePlayerSpawnAsync(CommandSender sender, String playerUUID, String playerName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "DELETE FROM player_spawns WHERE uuid = ? OR username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID);
                stmt.setString(2, playerName);
                stmt.executeUpdate();

                sender.sendRichMessage(config.getMessage("delspawn-success"));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error deleting spawn: " + e.getMessage());

                sender.sendRichMessage(config.getMessage("delspawn-fail"));
            }
        });
    }
}