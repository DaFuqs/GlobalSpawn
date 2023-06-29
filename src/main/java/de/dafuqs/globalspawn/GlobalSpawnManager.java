package de.dafuqs.globalspawn;

import net.minecraft.registry.*;
import net.minecraft.server.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

public class GlobalSpawnManager {
	
	private static @Nullable GlobalSpawnPoint globalRespawnPoint;
	private static @Nullable GlobalSpawnPoint initialSpawnPoint;
	
	// GENERAL
	public static void initialize(MinecraftServer server) {
		boolean shouldRespawnPointBeActive = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive;
		RegistryKey<World> globalSpawnWorldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnDimension));
		if (shouldRespawnPointBeActive && existsWorld(server, globalSpawnWorldKey)) {
			int x = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionX;
			int y = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionY;
			int z = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionZ;
			float angle = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnAngle;
			globalRespawnPoint = new GlobalSpawnPoint(globalSpawnWorldKey, new BlockPos(x, y, z), angle);
		}
		
		boolean shouldInitialSpawnPointBeActive = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive;
		RegistryKey<World> initialSpawnWorldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointDimension));
		if (shouldInitialSpawnPointBeActive && existsWorld(server, initialSpawnWorldKey)) {
			int x = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionX;
			int y = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionY;
			int z = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionZ;
			float angle = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnAngle;
			initialSpawnPoint = new GlobalSpawnPoint(initialSpawnWorldKey, new BlockPos(x, y, z), angle);
		}
	}
	
	private static boolean existsWorld(MinecraftServer server, RegistryKey<World> registryKey) {
		return server.getWorld(registryKey) != null;
	}
	
	private static void updateConfigFile() {
		// respawn point
		if (globalRespawnPoint != null) {
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive = true;
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnDimension = globalRespawnPoint.getDimension().getValue().toString();
			
			BlockPos globalRespawnPointSpawnBlockPos = globalRespawnPoint.getPos();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionX = globalRespawnPointSpawnBlockPos.getX();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionY = globalRespawnPointSpawnBlockPos.getY();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionZ = globalRespawnPointSpawnBlockPos.getZ();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnAngle = globalRespawnPoint.getAngle();
		} else {
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive = false;
		}
		
		// initial spawn point
		if (initialSpawnPoint != null) {
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive = true;
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointDimension = initialSpawnPoint.getDimension().getValue().toString();
			
			BlockPos initialSpawnPointSpawnBlockPos = initialSpawnPoint.getPos();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionX = initialSpawnPointSpawnBlockPos.getX();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionY = initialSpawnPointSpawnBlockPos.getY();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionZ = initialSpawnPointSpawnBlockPos.getZ();
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnAngle = initialSpawnPoint.getAngle();
		} else {
			GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive = false;
		}
		
		GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG_MANAGER.save();
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
			GlobalSpawnCommon.log(Level.WARN, "Respawn dimension " + globalRespawnPoint.getDimension() + " is not loaded. GlobalRespawn is disabled");
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
			GlobalSpawnCommon.log(Level.WARN, "Initial spawn dimension " + initialSpawnPoint.getDimension() + " is not loaded. InitialSpawn is disabled");
			return false;
		}
	}
	
}
