package de.dafuqs.globalspawn;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;

public class GlobalSpawnManager {

    private static final HashMap<RegistryKey<World>, World> dimensions = new HashMap<>();
    private static boolean respawnPointActive;
    private static GlobalSpawnPoint globalRespawnPoint;

    private static boolean initialSpawnActive;
    private static GlobalSpawnPoint initialSpawnPoint;

    public static Advancement advancement;

    // GENERAL
    public static void initialize() {
        respawnPointActive = false;
        initialSpawnActive = false;
    }

    public static boolean isNewPlayer(ServerPlayerEntity serverPlayerEntity) {
        if(advancement == null) {
            Identifier takingInventoryIdentifier = new Identifier("minecraft", "story/root");
            advancement = GlobalSpawnCommon.minecraftServer.getAdvancementLoader().get(takingInventoryIdentifier);
        }
        return serverPlayerEntity.getAdvancementTracker().getProgress(advancement).isDone();
    }

    public static void addWorld(World world) {
        dimensions.put(world.getRegistryKey(), world);

        Identifier identifier = new Identifier(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnDimension);
        boolean shouldRespawnPointBeActive = respawnPointActive = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive;
        if (shouldRespawnPointBeActive && world.getRegistryKey().getValue().equals(identifier)) {
            int x = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionX;
            int y = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionY;
            int z = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionZ;
            globalRespawnPoint = new GlobalSpawnPoint(world.getRegistryKey(), new BlockPos(x, y, z));
            respawnPointActive = true;
        }

        boolean shouldInitialSpawnPointBeActive = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive;
        identifier = new Identifier(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointDimension);
        if(shouldInitialSpawnPointBeActive && world.getRegistryKey().getValue().equals(identifier)) {
            int x = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionX;
            int y = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionY;
            int z = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionZ;
            initialSpawnPoint = new GlobalSpawnPoint(world.getRegistryKey(), new BlockPos(x, y, z));
            initialSpawnActive = true;
        }
    }

    private static boolean existsWorld(RegistryKey<World> registryKey) {
        return dimensions.containsKey(registryKey);
    }

    private static void updateConfigFile() {
        // respawn point
        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPointActive = respawnPointActive;
        if(globalRespawnPoint != null) {
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnDimension = globalRespawnPoint.getSpawnDimension().getValue().toString();

            BlockPos globalRespawnPointSpawnBlockPos = globalRespawnPoint.getSpawnBlockPos();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionX = globalRespawnPointSpawnBlockPos.getX();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionY = globalRespawnPointSpawnBlockPos.getY();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.globalRespawnPositionZ = globalRespawnPointSpawnBlockPos.getZ();
        }

        // initial spawn point
        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointActive = initialSpawnActive;
        if(initialSpawnPoint != null) {
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPointDimension = initialSpawnPoint.getSpawnDimension().getValue().toString();

            BlockPos initialSpawnPointSpawnBlockPos = initialSpawnPoint.getSpawnBlockPos();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionX = initialSpawnPointSpawnBlockPos.getX();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionY = initialSpawnPointSpawnBlockPos.getY();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.initialSpawnPositionZ = initialSpawnPointSpawnBlockPos.getZ();
        }

        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG_MANAGER.save();
    }

    // RESPAWN
    public static void setRespawnPoint(GlobalSpawnPoint globalRespawnPoint) {
        GlobalSpawnManager.globalRespawnPoint = globalRespawnPoint;
        respawnPointActive = true;
        updateConfigFile();
    }

    public static GlobalSpawnPoint getGlobalRespawnPoint() {
        if(respawnPointActive) {
            return globalRespawnPoint;
        } else {
            return null;
        }
    }

    public static void unsetRespawnPoint() {
        globalRespawnPoint = null;
        respawnPointActive = false;
        updateConfigFile();
    }

    public static boolean isGlobalRespawnPointActive() {
        if(respawnPointActive && globalRespawnPoint != null) {
            RegistryKey<World> spawnPointDimension = globalRespawnPoint.getSpawnDimension();

            if (existsWorld(spawnPointDimension)) {
                return true;
            } else {
                GlobalSpawnCommon.LOGGER.warn("[GlobalSpawn] Respawn dimension " + spawnPointDimension + " is not loaded. GlobalRespawn is disabled");
                return false;
            }
        } else {
            return false;
        }
    }

    // INITIAL SPAWN
    public static void setInitialSpawnPoint(GlobalSpawnPoint initialSpawnPoint) {
        GlobalSpawnManager.initialSpawnPoint = initialSpawnPoint;
        initialSpawnActive = true;
        updateConfigFile();
    }

    public static GlobalSpawnPoint getInitialSpawnPoint() {
        if(initialSpawnActive) {
            return initialSpawnPoint;
        } else {
            return null;
        }
    }

    public static void unsetInitialSpawnPoint() {
        initialSpawnPoint = null;
        initialSpawnActive = false;
        updateConfigFile();
    }

    public static boolean isInitialSpawnPointActive() {
        if(initialSpawnActive && initialSpawnPoint != null) {
            RegistryKey<World> spawnPointDimension = initialSpawnPoint.getSpawnDimension();

            if (existsWorld(spawnPointDimension)) {
                return true;
            } else {
                GlobalSpawnCommon.LOGGER.warn("[GlobalSpawn] Initial spawn dimension " + spawnPointDimension + " is not loaded. InitialSpawn is disabled");
                return false;
            }
        } else {
            return false;
        }
    }

}
