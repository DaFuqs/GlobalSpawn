package de.dafuqs.globalspawn;

import de.dafuqs.globalspawn.command.*;
import de.dafuqs.globalspawn.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import org.apache.logging.log4j.*;

public class GlobalSpawnCommon implements ModInitializer {
	
	public static final String MOD_ID = "globalspawn";
	public static ConfigManager<GlobalSpawnConfig> GLOBAL_SPAWN_CONFIG_MANAGER;
	public static GlobalSpawnConfig GLOBAL_SPAWN_CONFIG;
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
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
		
		ServerLifecycleEvents.SERVER_STARTED.register(server -> GlobalSpawnManager.initialize(server));
		
		log(Level.INFO, "Startup finished.");
	}
	
	public static void log(Level logLevel, String message) {
		LOGGER.log(logLevel, "[GlobalSpawn] " + message);
	}
	
}
