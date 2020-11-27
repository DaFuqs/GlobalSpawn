package de.dafuqs.globalspawn;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;

public class GlobalSpawnMixinHandler {

    /**
     * Handles checks for getRespawnDimension and getRespawnPosition
     * @param spawnPointDimension
     * @param spawnPointPosition
     * @param spawnPointSet
     * @return
     */
    public static GlobalSpawnPoint getRespawnData(RegistryKey<World> spawnPointDimension, BlockPos spawnPointPosition, boolean spawnPointSet) {
        if(GlobalSpawnManager.isActive()) {
            if (spawnPointDimension == World.OVERWORLD && spawnPointPosition == null) {
                return GlobalSpawnManager.get();
            }
        }
        return null;
    }

    /**
     * Handles the search for respawn position in the world itself
     * @param optional
     * @return
     */
    public static Optional<Vec3d> getRespawnPlayer(Optional<Vec3d> optional) {
        if(GlobalSpawnManager.isActive()) {
            if (!optional.isPresent()) {
                Vec3d vec3d = GlobalSpawnManager.get().getSpawnVec3D();
                return Optional.of(vec3d);
            }
        }
        return optional;
    }

    /**
     * Sets compound tags for the respawn position of new players
     * @param compoundTag
     * @return
     */
    public static CompoundTag modifySpawnRegistry(CompoundTag compoundTag) {
        // only for new players
        if(GlobalSpawnManager.isActive()) {
            if (compoundTag == null) {
                return GlobalSpawnManager.get().getSpawnCompoundTag();
            }
        }
        return compoundTag;
    }

    /**
     * Moving a newly joined player to the world spawn
     * @param serverPlayerEntity The player
     */
    public static boolean moveToSpawn(ServerPlayerEntity serverPlayerEntity) {
        if(GlobalSpawnManager.isActive()) {
            BlockPos spawnBlockPos = GlobalSpawnManager.get().getSpawnBlockPos();
            serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
            serverPlayerEntity.updatePosition(spawnBlockPos.getX(), spawnBlockPos.getY(), spawnBlockPos.getZ());
            return true;
        }
        return false;
    }

}
