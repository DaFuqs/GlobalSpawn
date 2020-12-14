package de.dafuqs.globalspawn;

import de.dafuqs.globalspawn.command.GlobalSpawnCommand;
import de.dafuqs.globalspawn.command.InitialSpawnCommand;
import de.dafuqs.globalspawn.config.GlobalSpawnConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder;
import me.sargunvohra.mcmods.autoconfig1u.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalSpawnCommon implements ModInitializer {

    public static final String MOD_ID = "globalspawn";
    public static ConfigManager GLOBAL_SPAWN_CONFIG_MANAGER;
    public static GlobalSpawnConfig GLOBAL_SPAWN_CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static MinecraftServer minecraftServer;

    @Override
    public void onInitialize() {
        //Set up config
        LOGGER.info("Loading config file...");
        ConfigHolder configHolder = AutoConfig.register(GlobalSpawnConfig.class, JanksonConfigSerializer::new);
        GLOBAL_SPAWN_CONFIG_MANAGER = ((ConfigManager) configHolder);
        GLOBAL_SPAWN_CONFIG = AutoConfig.getConfigHolder(GlobalSpawnConfig.class).getConfig();
        LOGGER.info("Finished loading config file.");

        GlobalSpawnCommand.register();
        InitialSpawnCommand.register();
        GlobalSpawnManager.initialize();

        ServerWorldEvents.LOAD.register((server, world) -> {
            GlobalSpawnCommon.minecraftServer = server;
            GlobalSpawnManager.addWorld(world);
        });
    }

}
