package com.umnirium.mc.teleportway;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandHandler {
    private final ConfigManager config;
    private final DatabaseManager dbManager;
    private final TeleportManager tpManager;

    private final SuggestionProvider<CommandSourceStack> playerSuggestions =
            (context, builder) -> {
                Bukkit.getOnlinePlayers().forEach(player ->
                        builder.suggest(player.getName())
                );
                return builder.buildFuture();
    };

    public CommandHandler(ConfigManager config, DatabaseManager dbManager, TeleportManager tpManager) {
        this.config = config;
        this.dbManager = dbManager;
        this.tpManager = tpManager;
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

                            dbManager.getPlayerSpawnAsync(player, location -> tpManager.tpSpawn(player, location));

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(
                                Commands.argument("player", StringArgumentType.word())
                                        .suggests(playerSuggestions)
                                        .requires(source -> source.getSender().hasPermission("teleportway.command.spawn.others"))
                                        .executes(ctx -> {
                                            String target = ctx.getArgument("player", String.class);

                                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                                return Command.SINGLE_SUCCESS;
                                            }

                                            dbManager.getPlayerSpawnAsync(target, location -> tpManager.tpSpawn(player, location, target));

                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .build(),
                "Teleport to spawn",
                List.of("tspawn", "twspawn", "wayspawn")
        );

        commands.register(
                Commands.literal("delspawn")
                        .requires(source -> source.getSender().hasPermission("teleportway.command.delspawn"))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                return Command.SINGLE_SUCCESS;
                            }

                            dbManager.deletePlayerSpawnAsync(player);

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(
                                Commands.argument("player", StringArgumentType.word())
                                        .suggests(playerSuggestions)
                                        .requires(source -> source.getSender().hasPermission("teleportway.command.delspawn.others"))
                                        .executes(ctx -> {
                                            String target = ctx.getArgument("player", String.class);

                                            dbManager.deletePlayerSpawnAsync(ctx.getSource().getSender(), target);

                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
                        .build(),
                "Deletes spawn",
                List.of("tdelspawn", "twdelspawn", "waydelspawn")
        );
    }
}