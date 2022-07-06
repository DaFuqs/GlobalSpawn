package de.dafuqs.globalspawn;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;

public class GlobalSpawnMixinHandler {
	
	/**
	 * Sets compound tags for the spawn position of new players
	 * <p>
	 * CompoundTag is null when players first join => modify
	 * The tag is not really set to the player (so not permanent)
	 * but used to position the player in the world on spawn
	 *
	 * @param nbtCompound The NBTag of a connecting player
	 * @return CompoundTag with modified spawn position and dimension
	 */
	public static NbtCompound modifySpawnRegistryPositionAndDimensionForNewPlayer(NbtCompound nbtCompound) {
		if (GlobalSpawnManager.isInitialSpawnPointActive()) {
			return GlobalSpawnManager.getInitialSpawnPoint().getSpawnNbtCompound(nbtCompound);
		} else if (GlobalSpawnManager.isGlobalRespawnPointActive()) {
			return GlobalSpawnManager.getGlobalRespawnPoint().getSpawnNbtCompound(nbtCompound);
		}
		return nbtCompound;
	}
	
	/**
	 * Sets compound tags for the spawn position of existing players
	 * <p>
	 * The tag is not really set to the player (so not permanent)
	 * but used to position the player in the world on spawn
	 *
	 * @param nbtCompound The NBTag of a connecting player
	 * @return CompoundTag with modified spawn position and dimension
	 */
	public static NbtCompound modifySpawnRegistryPositionAndDimensionForExistingPlayer(NbtCompound nbtCompound) {
		if (GlobalSpawnManager.isGlobalRespawnPointActive()) {
			return GlobalSpawnManager.getGlobalRespawnPoint().getSpawnNbtCompound(nbtCompound);
		} else {
			return nbtCompound;
		}
	}
	
	/**
	 * Moving a newly joined player to the world spawn
	 *
	 * @param serverPlayerEntity The player
	 */
	public static boolean movePlayerToSpawn(ServerPlayerEntity serverPlayerEntity) {
		if (GlobalSpawnManager.isInitialSpawnPointActive() && isNewPlayer(serverPlayerEntity)) {
			BlockPos spawnBlockPos = GlobalSpawnManager.getInitialSpawnPoint().getSpawnBlockPos();
			serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
			serverPlayerEntity.updatePosition(spawnBlockPos.getX() + 0.5F, spawnBlockPos.getY(), spawnBlockPos.getZ() + 0.5F);
			return true;
		} else if (GlobalSpawnManager.isGlobalRespawnPointActive()) {
			BlockPos spawnBlockPos = GlobalSpawnManager.getGlobalRespawnPoint().getSpawnBlockPos();
			serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
			serverPlayerEntity.updatePosition(spawnBlockPos.getX() + 0.5F, spawnBlockPos.getY(), spawnBlockPos.getZ() + 0.5F);
			return true;
		}
		return false;
	}
	
	public static boolean isNewPlayer(ServerPlayerEntity serverPlayerEntity) {
		Stat deathsStat = Stats.CUSTOM.getOrCreateStat(Stats.DEATHS);
		Stat walkedStat = Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM);
		int deaths = serverPlayerEntity.getStatHandler().getStat(deathsStat);
		int walked = serverPlayerEntity.getStatHandler().getStat(walkedStat);
		return deaths == 0 && walked == 0;
	}
	
}
