package de.dafuqs.globalspawn;

import de.dafuqs.globalspawn.command.*;
import de.dafuqs.globalspawn.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import org.slf4j.*;

public class GlobalSpawnCommon implements ModInitializer {
	
	public static final String MOD_ID = "globalspawn";
	public static ConfigHolder<GlobalSpawnConfig> GLOBAL_SPAWN_CONFIG_HOLDER;
	public static GlobalSpawnConfig GLOBAL_SPAWN_CONFIG;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	@Override
	public void onInitialize() {
		//Set up config
		LOGGER.info("Loading config file...");
        GLOBAL_SPAWN_CONFIG_HOLDER = AutoConfig.register(GlobalSpawnConfig.class, JanksonConfigSerializer::new);
		GLOBAL_SPAWN_CONFIG = GLOBAL_SPAWN_CONFIG_HOLDER.getConfig();

		LOGGER.info("Registering Spawn Override...");
		GlobalSpawnCommand.register();
		InitialSpawnCommand.register();
		
		ServerLifecycleEvents.SERVER_STARTED.register(GlobalSpawnManager::initialize);

		LOGGER.info("Startup finished.");
	}

}
