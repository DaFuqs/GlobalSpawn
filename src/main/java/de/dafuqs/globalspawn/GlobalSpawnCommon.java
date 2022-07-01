package de.dafuqs.globalspawn;

import de.dafuqs.globalspawn.command.GlobalSpawnCommand;
import de.dafuqs.globalspawn.command.InitialSpawnCommand;
import de.dafuqs.globalspawn.config.GlobalSpawnConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalSpawnCommon implements ModInitializer {
	
	public static final String MOD_ID = "globalspawn";
	public static ConfigManager<GlobalSpawnConfig> GLOBAL_SPAWN_CONFIG_MANAGER;
	public static GlobalSpawnConfig GLOBAL_SPAWN_CONFIG;
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	public static MinecraftServer minecraftServer;
	
	@Override
	public void onInitialize() {
		//Set up config
		log(Level.INFO, "Loading config file...");
		ConfigHolder<GlobalSpawnConfig> configHolder = AutoConfig.register(GlobalSpawnConfig.class, JanksonConfigSerializer::new);
		GLOBAL_SPAWN_CONFIG_MANAGER = ((ConfigManager<GlobalSpawnConfig>) configHolder);
		GLOBAL_SPAWN_CONFIG = AutoConfig.getConfigHolder(GlobalSpawnConfig.class).getConfig();
		
		log(Level.INFO, "Registering Spawn Override...");
		GlobalSpawnCommand.register();
		InitialSpawnCommand.register();
		GlobalSpawnManager.initialize();
		
		ServerWorldEvents.LOAD.register((server, world) -> {
			GlobalSpawnCommon.minecraftServer = server;
			GlobalSpawnManager.addWorld(world);
		});
		
		log(Level.INFO, "Startup finished.");
	}
	
	public static void log(Level logLevel, String message) {
		LOGGER.log(logLevel, "[GlobalSpawn] " + message);
	}
	
}
