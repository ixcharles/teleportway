package com.umnirium.mc.teleportway;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class CommandHandler {
    private final ConfigManager config;
    private final DatabaseManager dbManager;

    public CommandHandler(ConfigManager config, DatabaseManager dbManager) {
        this.config = config;
        this.dbManager = dbManager;
    }

    public void register(Commands commands, JavaPlugin plugin) {
        commands.register(
                Commands.literal("setspawn")
                        .requires(source -> source.getSender().hasPermission("teleportway.command.setspawn"))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                return Command.SINGLE_SUCCESS;
                            }

                            Location location = player.getLocation();

                            dbManager.savePlayerSpawnAsync(player, location);

                            return Command.SINGLE_SUCCESS;
                        }).build(),
                "Set custom spawn",
                List.of("tsetspawn", "twsetspawn", "waysetspawn")
        );

        commands.register(
                Commands.literal("spawn")
                        .requires(source -> source.getSender().hasPermission("teleportway.command.spawn"))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                return Command.SINGLE_SUCCESS;
                            }

                            dbManager.getPlayerSpawnAsync(player, location ->  {
                                if (location == null) {
                                    player.sendRichMessage(config.getMessage("no-spawn"));

                                    World world = Bukkit.getWorld("world");

                                    Objects.requireNonNull(world).getChunkAtAsync(world.getSpawnLocation()).thenRun(() -> {
                                        player.sendRichMessage(config.getMessage("teleport-try"));
                                        player.teleportAsync(world.getSpawnLocation()).thenAccept(success -> player.sendRichMessage(success ? config.getMessage("teleport-success") : config.getMessage("teleport-fail")));
                                    });
                                }

                                else {
                                    Objects.requireNonNull(location).getWorld().getChunkAtAsync(location).thenRun(() -> {
                                        player.sendRichMessage(config.getMessage("teleport-try"));
                                        player.teleportAsync(location).thenAccept(success -> player.sendRichMessage(success ? config.getMessage("teleport-success") : config.getMessage("teleport-fail")));
                                    });
                                }
                            });

                            return Command.SINGLE_SUCCESS;
                        }).build(),
                "Teleport to spawn",
                List.of("tspawn", "twspawn", "wayspawn")
        );
    }
}