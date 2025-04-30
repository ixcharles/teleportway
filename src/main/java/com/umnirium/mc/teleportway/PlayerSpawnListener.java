package com.umnirium.mc.teleportway;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {
    private final DatabaseManager dbManager;

    public PlayerSpawnListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();

        dbManager.getPlayerSpawnAsync(player, location -> {
            if (location != null) {
                player.setRespawnLocation(location, true);
            }
        });
    }

    @EventHandler
    public void onPlayeSpawnSet(PlayerSetSpawnEvent event) {
        Player player = event.getPlayer();

        dbManager.savePlayerSpawnAsync(player, event.getLocation());
    }
}
