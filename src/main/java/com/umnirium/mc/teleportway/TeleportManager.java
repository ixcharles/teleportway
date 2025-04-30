package com.umnirium.mc.teleportway;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

public class TeleportManager {
    private final ConfigManager config;

    public TeleportManager(ConfigManager config) {
        this.config = config;
    }

    public void teleportAsync(Player player, World world, Location location) {
        Objects.requireNonNull(world).getChunkAtAsync(location).thenRun(() -> {
            player.sendRichMessage(config.getMessage("teleport-try"));

            boolean success = player.teleport(location);
            player.sendRichMessage(success ? config.getMessage("teleport-success") : config.getMessage("teleport-fail"));
        });
    }

    public void tpSpawn(Player player, Location location) {
        if (location == null) {
            player.sendRichMessage(config.getMessage("no-spawn"));

            World world = Bukkit.getWorlds().getFirst();

            teleportAsync(player, world, world.getSpawnLocation());
        }

        else {
            teleportAsync(player, location.getWorld(), location);
        }
    }

    public void tpSpawn(Player player, Location location, String target) {
        if (location == null) {
            player.sendRichMessage(config.getMessage("no-spawn-others").replace("%player%", target));
        }

        else {
            teleportAsync(player, location.getWorld(), location);
        }
    }
}
