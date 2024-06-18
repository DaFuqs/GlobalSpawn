package de.dafuqs.globalspawn;

import de.dafuqs.globalspawn.command.*;
import de.dafuqs.globalspawn.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.command.v2.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.command.argument.serialize.*;
import net.minecraft.util.*;
import org.slf4j.*;

public class GlobalSpawn implements ModInitializer {
	
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

		LOGGER.info("Registering Commands...");
		ArgumentTypeRegistry.registerArgumentType(
				Identifier.of(MOD_ID, "spawn_criterion"),
				SpawnCriterionArgumentType.class,
				ConstantArgumentSerializer.of(SpawnCriterionArgumentType::criterion)
		);
		GlobalSpawnCommand.register();
		InitialSpawnCommand.register();

		ServerLifecycleEvents.SERVER_STARTED.register(GlobalSpawnManager::initialize);

		LOGGER.info("Startup finished!");
	}

}
