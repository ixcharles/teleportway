package com.umnirium.mc.teleportway;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportWay extends JavaPlugin {
    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        getComponentLogger().info(mm.deserialize("<aqua>[WayGate]</aqua> <white>Plugin successfully enabled</white>"));
        getComponentLogger().info(mm.deserialize("<aqua>[WayGate]</aqua> <white>Consider supporting here:</white> <yellow><click:open_url:'https://ko-fi.com/H2H61DN2C9'>https://ko-fi.com/H2H61DN2C9</click></yellow>"));
    }

    @Override
    public void onDisable() {
        getComponentLogger().info(mm.deserialize("<aqua>[WayGate]</aqua> <white>Plugin successfully disabled</white>"));
    }
}