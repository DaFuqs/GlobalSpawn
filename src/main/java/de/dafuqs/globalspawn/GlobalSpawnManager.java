package de.dafuqs.globalspawn;

import net.minecraft.registry.*;
import net.minecraft.server.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class GlobalSpawnManager {
	
	private static @Nullable GlobalSpawnPoint globalRespawnPoint;
	private static @Nullable GlobalSpawnPoint initialSpawnPoint;
	
	// GENERAL
	public static void initialize(MinecraftServer server) {
		boolean shouldRespawnPointBeActive = GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive;
		RegistryKey<World> globalSpawnWorldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnDimension));
		if (shouldRespawnPointBeActive && existsWorld(server, globalSpawnWorldKey)) {
			int x = GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionX;
			int y = GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionY;
			int z = GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionZ;
			int spread = GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionSpread;
			GlobalSpawnPoint.SpawnCriterion criterion = GlobalSpawnPoint.SpawnCriterion.valueOf(GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnCriterion.toUpperCase(Locale.ROOT));
			float angle = GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnAngle;
			globalRespawnPoint = new GlobalSpawnPoint(globalSpawnWorldKey, new BlockPos(x, y, z), spread, criterion, angle);
		}

		boolean shouldInitialSpawnPointBeActive = GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive;
		RegistryKey<World> initialSpawnWorldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPointDimension));
		if (shouldInitialSpawnPointBeActive && existsWorld(server, initialSpawnWorldKey)) {
			int x = GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionX;
			int y = GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionY;
			int z = GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionZ;
			int spread = GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionSpread;
			GlobalSpawnPoint.SpawnCriterion criterion = GlobalSpawnPoint.SpawnCriterion.valueOf(GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnCriterion.toUpperCase(Locale.ROOT));
			float angle = GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnAngle;
			initialSpawnPoint = new GlobalSpawnPoint(initialSpawnWorldKey, new BlockPos(x, y, z), spread, criterion, angle);
		}
	}
	
	private static boolean existsWorld(MinecraftServer server, RegistryKey<World> registryKey) {
		return server.getWorld(registryKey) != null;
	}
	
	private static void updateConfigFile() {
		// respawn point
		if (globalRespawnPoint != null) {
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive = true;
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnDimension = globalRespawnPoint.getDimension().getValue().toString();
			
			BlockPos globalRespawnPointSpawnBlockPos = globalRespawnPoint.getPos();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionX = globalRespawnPointSpawnBlockPos.getX();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionY = globalRespawnPointSpawnBlockPos.getY();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionZ = globalRespawnPointSpawnBlockPos.getZ();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPositionSpread = globalRespawnPoint.getHorizontalSpread();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnCriterion = globalRespawnPoint.getCriterion().toString().toLowerCase(Locale.ROOT);
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnAngle = globalRespawnPoint.getAngle();
		} else {
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive = false;
		}
		
		// initial spawn point
		if (initialSpawnPoint != null) {
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive = true;
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPointDimension = initialSpawnPoint.getDimension().getValue().toString();
			
			BlockPos initialSpawnPointSpawnBlockPos = initialSpawnPoint.getPos();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionX = initialSpawnPointSpawnBlockPos.getX();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionY = initialSpawnPointSpawnBlockPos.getY();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionZ = initialSpawnPointSpawnBlockPos.getZ();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPositionSpread = initialSpawnPoint.getHorizontalSpread();
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnCriterion = initialSpawnPoint.getCriterion().toString().toLowerCase(Locale.ROOT);
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnAngle = initialSpawnPoint.getAngle();
		} else {
			GlobalSpawn.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive = false;
		}

		GlobalSpawn.GLOBAL_SPAWN_CONFIG_HOLDER.save();
	}
	
	// RESPAWN
	public static void setGlobalSpawnPoint(GlobalSpawnPoint globalRespawnPoint) {
		GlobalSpawnManager.globalRespawnPoint = globalRespawnPoint;
		updateConfigFile();
	}
	
	public static @Nullable GlobalSpawnPoint getGlobalRespawnPoint() {
		return globalRespawnPoint;
	}
	
	public static void unsetGlobalSpawnPoint() {
		globalRespawnPoint = null;
		updateConfigFile();
	}
	
	public static boolean isGlobalSpawnPointActive(MinecraftServer server) {
		if (globalRespawnPoint == null) {
			return false;
		}
		
		if (existsWorld(server, globalRespawnPoint.getDimension())) {
			return true;
		} else {
			GlobalSpawn.LOGGER.warn("Respawn dimension {} is not loaded. GlobalRespawn is disabled", globalRespawnPoint.getDimension());
			return false;
		}
	}
	
	// INITIAL SPAWN
	public static void setInitialSpawnPoint(GlobalSpawnPoint initialSpawnPoint) {
		GlobalSpawnManager.initialSpawnPoint = initialSpawnPoint;
		updateConfigFile();
	}
	
	public static @Nullable GlobalSpawnPoint getInitialSpawnPoint() {
		return initialSpawnPoint;
	}
	
	public static void unsetInitialSpawnPoint() {
		initialSpawnPoint = null;
		updateConfigFile();
	}
	
	public static boolean isInitialSpawnPointActive(MinecraftServer server) {
		if (initialSpawnPoint == null) {
			return false;
		}
		
		if (existsWorld(server, initialSpawnPoint.getDimension())) {
			return true;
		} else {
			GlobalSpawn.LOGGER.warn("Initial spawn dimension {} is not loaded. InitialSpawn is disabled", initialSpawnPoint.getDimension());
			return false;
		}
	}
	
}
