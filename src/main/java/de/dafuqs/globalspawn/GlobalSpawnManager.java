package de.dafuqs.globalspawn;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;

public class GlobalSpawnManager {

    private static final HashMap<RegistryKey<World>, World> dimensions = new HashMap<>();
    private static boolean active;
    private static GlobalSpawnPoint activeSpawnPointDefinition;

    public static void unset() {
        activeSpawnPointDefinition = null;
        active = false;
        updateConfigFile();
    }

    public static void set(GlobalSpawnPoint globalSpawnPoint) {
        activeSpawnPointDefinition = globalSpawnPoint;
        active = true;
        updateConfigFile();
    }

    private static void updateConfigFile() {
        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.active = active;
        if(activeSpawnPointDefinition != null) {
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnDimension = activeSpawnPointDefinition.getSpawnDimension().getValue().toString();

            BlockPos spawnPointPosition = activeSpawnPointDefinition.getSpawnBlockPos();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnX = spawnPointPosition.getX();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnY = spawnPointPosition.getY();
            GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.spawnZ = spawnPointPosition.getZ();
        }

        GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG_MANAGER.save();
    }

    public static GlobalSpawnPoint getGlobalSpawnPoint() {
        if(active) {
            return activeSpawnPointDefinition;
        } else {
            return null;
        }
    }

    public static boolean isActive() {
        if(active && activeSpawnPointDefinition != null) {
            RegistryKey<World> spawnPointDimension = activeSpawnPointDefinition.getSpawnDimension();

            if (existsWorld(spawnPointDimension)) {
                return true;
            } else {
                GlobalSpawnCommon.LOGGER.warn("Spawn dimension " + spawnPointDimension + " is not loaded. GlobalSpawn is disabled");
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean existsWorld(RegistryKey<World> registryKey) {
        return dimensions.containsKey(registryKey);
    }

    public static ServerWorld getWorld(RegistryKey<World> registryKey) {
        return (ServerWorld) dimensions.get(registryKey);
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
