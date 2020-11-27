package de.dafuqs.globalspawn;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;

public class GlobalSpawnManager {

    private static HashMap<RegistryKey<World>, World> dimensions = new HashMap<>();
    private static boolean active;
    private static GlobalSpawnPoint activeSpawnPointDefinition;

    public static void unset() {
        activeSpawnPointDefinition = null;
        active = false;
        updateConfigFile();
    }

    public static void set(GlobalSpawnPoint globalSpawnPoint) {
        activeSpawnPointDefinition = globalSpawnPoint;
        updateConfigFile();
    }

    private static void updateConfigFile() {
        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.active = active;
        if(activeSpawnPointDefinition != null) {
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnDimension = activeSpawnPointDefinition.spawnPointDimension.getValue().toString();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnX = activeSpawnPointDefinition.spawnPointPosition.getX();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnY = activeSpawnPointDefinition.spawnPointPosition.getY();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnZ = activeSpawnPointDefinition.spawnPointPosition.getZ();
        }

        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG_MANAGER.save();
    }

    public static GlobalSpawnPoint get() {
        return activeSpawnPointDefinition;
    }

    public static boolean isActive() {
        if(active && activeSpawnPointDefinition != null) {
            if (existsWorld(activeSpawnPointDefinition.spawnPointDimension)) {
                return true;
            } else {
                GlobalSpawnCommon.LOGGER.warn("Spawn dimension " + activeSpawnPointDefinition.spawnPointDimension + " is not loaded. GlobalSpawn is disabled");
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean existsWorld(RegistryKey<World> registryKey) {
        return dimensions.containsKey(registryKey);
    }

    public static void initialize() {
        active = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.active;
    }

    public static void addWorld(World world) {
        dimensions.put(world.getRegistryKey(), world);

        boolean shouldBeActive = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.active;
        if(shouldBeActive) {
            Identifier identifier = new Identifier(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnDimension);
            if(world.getRegistryKey().getValue().equals(identifier)) {
                int x = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnX;
                int y = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnY;
                int z = GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnZ;
                activeSpawnPointDefinition = new GlobalSpawnPoint(world.getRegistryKey(), new BlockPos(x, y, z));
            }
        }
    }

}
