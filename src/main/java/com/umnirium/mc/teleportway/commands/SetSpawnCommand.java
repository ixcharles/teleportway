package com.umnirium.mc.teleportway.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.umnirium.mc.teleportway.ConfigManager;
import com.umnirium.mc.teleportway.DatabaseManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SetSpawnCommand {
    private final ConfigManager config;
    private final DatabaseManager dbManager;

    private final SuggestionProvider<CommandSourceStack> playerSuggestions =
            (context, builder) -> {
                Bukkit.getOnlinePlayers().forEach(player ->
                        builder.suggest(player.getName())
                );
                return builder.buildFuture();
            };

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
                            player.setRespawnLocation(location, true);

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(
                                Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(
                                                Commands.argument("y", DoubleArgumentType.doubleArg())
                                                        .then(
                                                                Commands.argument("z", DoubleArgumentType.doubleArg())
                                                                        .requires(source -> source.getSender().hasPermission("teleportway.command.setspawn.position"))
                                                                        .executes(ctx -> {
                                                                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                                                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                                                                return Command.SINGLE_SUCCESS;
                                                                            }

                                                                            Location location = new Location(
                                                                                    player.getWorld(),
                                                                                    ctx.getArgument("x", Double.class),
                                                                                    ctx.getArgument("y", Double.class),
                                                                                    ctx.getArgument("z", Double.class),
                                                                                    player.getYaw(),
                                                                                    player.getPitch()
                                                                            );

                                                                            dbManager.savePlayerSpawnAsync(player, location);
                                                                            player.setRespawnLocation(location, true);

                                                                            return Command.SINGLE_SUCCESS;
                                                                        })
                                                        )
                                        )

                        )
                        .then(
                                Commands.argument("player", StringArgumentType.word())
                                        .suggests(playerSuggestions)
                                        .requires(source -> source.getSender().hasPermission("teleportway.command.setspawn.others"))
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String target = ctx.getArgument("player", String.class);
                                            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

                                            Location location = player.getLocation();

                                            dbManager.savePlayerSpawnAsync(ctx.getSource().getSender(), targetPlayer, location);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(
                                                Commands.argument("x", DoubleArgumentType.doubleArg())
                                                        .then(
                                                                Commands.argument("y", DoubleArgumentType.doubleArg())
                                                                        .then(
                                                                                Commands.argument("z", DoubleArgumentType.doubleArg())
                                                                                        .requires(source -> source.getSender().hasPermission("teleportway.command.setspawn.others.position"))
                                                                                        .executes(ctx -> {
                                                                                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                                                                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                                                                                return Command.SINGLE_SUCCESS;
                                                                                            }

                                                                                            String target = ctx.getArgument("player", String.class);
                                                                                            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

                                                                                            Location location = new Location(
                                                                                                    player.getWorld(),
                                                                                                    ctx.getArgument("x", Double.class),
                                                                                                    ctx.getArgument("y", Double.class),
                                                                                                    ctx.getArgument("z", Double.class),
                                                                                                    player.getYaw(),
                                                                                                    player.getPitch()
                                                                                            );

                                                                                            dbManager.savePlayerSpawnAsync(ctx.getSource().getSender(), targetPlayer, location);

                                                                                            return Command.SINGLE_SUCCESS;
                                                                                        })
                                                                        )
                                                        )
                                        )
                        )
                        .build(),
                "Set custom spawn",
                List.of("tsetspawn", "twsetspawn", "waysetspawn")
        );
    }
}