package com.umnirium.mc.teleportway;

import com.umnirium.mc.teleportway.commands.DelSpawnCommand;
import com.umnirium.mc.teleportway.commands.SetSpawnCommand;
import com.umnirium.mc.teleportway.commands.SpawnCommand;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class TeleportWay extends JavaPlugin {
    MiniMessage mm = MiniMessage.miniMessage();
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigManager config = new ConfigManager();
        TeleportManager tpManager = new TeleportManager(config);
        dbManager = new DatabaseManager(this, config);

        config.createMessagesFile();

        Bukkit.getPluginManager().registerEvents(new PlayerSpawnListener(dbManager), this);

        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new DelSpawnCommand(config, dbManager).register(commands, this);
            new SetSpawnCommand(config, dbManager).register(commands, this);
            new SpawnCommand(config, dbManager, tpManager).register(commands, this);
        });

        getComponentLogger().info(mm.deserialize("<aqua>Plugin successfully enabled</aqua>"));
        getComponentLogger().info(mm.deserialize("<white>Consider supporting here:</white> <yellow><click:open_url:'https://ko-fi.com/H2H61DN2C9'>https://ko-fi.com/H2H61DN2C9</click></yellow>"));
    }

    @Override
    public void onDisable() {
        dbManager.closeConnection();

        getComponentLogger().info(mm.deserialize("<aqua>Plugin successfully disabled</aqua>"));
    }
}