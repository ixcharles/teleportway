package com.umnirium.mc.teleportway.commands;

import com.mojang.brigadier.Command;
import com.umnirium.mc.teleportway.ConfigManager;
import com.umnirium.mc.teleportway.DatabaseManager;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SetSpawnCommand {
    private final ConfigManager config;
    private final DatabaseManager dbManager;

    public SetSpawnCommand(ConfigManager config, DatabaseManager dbManager) {
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
    }
}