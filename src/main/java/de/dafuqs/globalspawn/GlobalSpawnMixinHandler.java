package de.dafuqs.globalspawn;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class GlobalSpawnMixinHandler {

    /**
     * Handles checks for getRespawnDimension and getRespawnPosition
     *
     * Defaults (vanilla):
     * Spawn Point never set: Dimension = Overworld
     *                        Position  = null
     *
     * Respawn Anchor used:   Dimension = Nether
     *                        Position  = Position of Anchor (can be broken in the meantime / empty!)
     *
     * @param playerSpawnPointDimension The ServerPlayerEntities property "spawnPointPosition" (can be null if never set)
     * @param playerSpawnPointPosition The ServerPlayerEntities property "spawnPointDimension". Always set (default is OVERWORLD)
     * @param hasPlayerSpawnPointSet The ServerPlayerEntities property "spawnPointSet". IS ONLY TRUE WHEN THE SPAWN POINT IS SET VIA COMMAND. The respawn anchor sets it to false!
     * @return The new spawn point
     */
    public static GlobalSpawnPoint setRespawningPlayersDataWithoutSpawnPoint(RegistryKey<World> playerSpawnPointDimension, BlockPos playerSpawnPointPosition, boolean hasPlayerSpawnPointSet) {
        if(GlobalSpawnManager.isGlobalRespawnPointActive()) {
            // player has spawn point set via /spawnPoint command => don't handle that one
            if(hasPlayerSpawnPointSet && playerSpawnPointPosition != null) {
                // spawn point command used => vanilla behavior
                return null;
            } else if (playerSpawnPointPosition == null) {
                // no spawn set => use global
                return GlobalSpawnManager.getGlobalRespawnPoint();
            } else {
                // vanilla spawn set => try...
                // if bed obstructed / respawn anchor empty etc: we have to catch it later
                return null;
            }
        } else {
            //inactive => vanilla behavior
            return null;
        }
    }

    /**
     * Sets compound tags for the spawn position of new players
     *
     * CompoundTag is null when players first join => modify
     * The tag is not really set to the player (so not permanent)
     * but used to position the player in the world on spawn
     *
     * @param compoundTag The NBTag of a connecting player
     * @return CompoundTag with modified spawn position and dimension
     */
    public static NbtCompound modifySpawnRegistryPositionAndDimensionForNewPlayer(NbtCompound compoundTag) {
        // triggers only for new players
        if (compoundTag == null) {
            // new player => Add spawn tag
            if(GlobalSpawnManager.isInitialSpawnPointActive()) {
                return GlobalSpawnManager.getInitialSpawnPoint().getSpawnNbtCompound();
            } else if(GlobalSpawnManager.isGlobalRespawnPointActive()) {
                return GlobalSpawnManager.getGlobalRespawnPoint().getSpawnNbtCompound();
            }
        }
        return compoundTag;
    }

    /**
     * Moving a newly joined player to the world spawn
     * @param serverPlayerEntity The player
     */
    public static boolean movePlayerToSpawn(ServerPlayerEntity serverPlayerEntity) {
        if(isNewPlayer(serverPlayerEntity) && GlobalSpawnManager.isInitialSpawnPointActive()) {
            BlockPos spawnBlockPos = GlobalSpawnManager.getInitialSpawnPoint().getSpawnBlockPos();
            serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
            serverPlayerEntity.updatePosition(spawnBlockPos.getX() + 0.5F, spawnBlockPos.getY(), spawnBlockPos.getZ() + 0.5F);
            return true;
        } else if(GlobalSpawnManager.isGlobalRespawnPointActive()) {
            BlockPos spawnBlockPos = GlobalSpawnManager.getGlobalRespawnPoint().getSpawnBlockPos();
            serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
            serverPlayerEntity.updatePosition(spawnBlockPos.getX() + 0.5F, spawnBlockPos.getY(), spawnBlockPos.getZ() + 0.5F);
            return true;
        }
        return false;
    }

    private static boolean isNewPlayer(ServerPlayerEntity serverPlayerEntity) {
        Stat deathsStat = Stats.CUSTOM.getOrCreateStat(Stats.DEATHS);
        int deaths = serverPlayerEntity.getStatHandler().getStat(deathsStat);
        return deaths == 0;
    }

}
