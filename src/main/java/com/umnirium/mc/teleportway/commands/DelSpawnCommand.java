package com.umnirium.mc.teleportway.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.umnirium.mc.teleportway.ConfigManager;
import com.umnirium.mc.teleportway.DatabaseManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class DelSpawnCommand {
    private final ConfigManager config;
    private final DatabaseManager dbManager;

    private final SuggestionProvider<CommandSourceStack> playerSuggestions =
            (context, builder) -> {
                Bukkit.getOnlinePlayers().forEach(player ->
                        builder.suggest(player.getName())
                );
                return builder.buildFuture();
            };

    public DelSpawnCommand(ConfigManager config, DatabaseManager dbManager) {
        this.config = config;
        this.dbManager = dbManager;
    }

    public void register(Commands commands, JavaPlugin plugin) {
        commands.register(
                Commands.literal("delspawn")
                        .requires(source -> source.getSender().hasPermission("teleportway.command.delspawn"))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendRichMessage(config.getMessage("no-console"));

                                return Command.SINGLE_SUCCESS;
                            }

                            dbManager.deletePlayerSpawnAsync(player);
                            player.setRespawnLocation(null, false);

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