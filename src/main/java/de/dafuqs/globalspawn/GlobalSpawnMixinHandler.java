package de.dafuqs.globalspawn;

import net.minecraft.nbt.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.stat.*;
import net.minecraft.util.math.*;

public class GlobalSpawnMixinHandler {
	
	/**
	 * Sets compound tags for the spawn position of new players
	 * <p>
	 * CompoundTag is null when players first join => modify
	 * The tag is not really set to the player (so not permanent)
	 * but used to position the player in the world on spawn
	 *
	 * @param nbtCompound The NBTag of a connecting player
	 */
	public static NbtCompound modifySpawnRegistryPositionAndDimensionForNewPlayer(MinecraftServer server, NbtCompound nbtCompound) {
		if (GlobalSpawnManager.isInitialSpawnPointActive(server)) {
			return GlobalSpawnManager.getInitialSpawnPoint().getSpawnNbtCompound(nbtCompound);
		} else if (GlobalSpawnManager.isGlobalSpawnPointActive(server)) {
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
	public static NbtCompound modifySpawnRegistryPositionAndDimensionForExistingPlayer(MinecraftServer server, NbtCompound nbtCompound) {
		if (GlobalSpawnManager.isGlobalSpawnPointActive(server)) {
			return GlobalSpawnManager.getGlobalRespawnPoint().getSpawnNbtCompound(nbtCompound);
		} else {
			return nbtCompound;
		}
	}
	
	/**
	 * Moving a newly joined player to the world spawn
	 * @param serverPlayerEntity The player
	 */
	public static boolean movePlayerToSpawn(ServerPlayerEntity serverPlayerEntity) {
		if (GlobalSpawnManager.isInitialSpawnPointActive(serverPlayerEntity.server) && isNewPlayer(serverPlayerEntity)) {
			BlockPos spawnBlockPos = GlobalSpawnManager.getInitialSpawnPoint().getPos();
			serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
			serverPlayerEntity.updatePosition(spawnBlockPos.getX() + 0.5F, spawnBlockPos.getY(), spawnBlockPos.getZ() + 0.5F);
			return true;
		} else if (GlobalSpawnManager.isGlobalSpawnPointActive(serverPlayerEntity.server)) {
			BlockPos spawnBlockPos = GlobalSpawnManager.getGlobalRespawnPoint().getPos();
			serverPlayerEntity.refreshPositionAndAngles(spawnBlockPos, 0.0F, 0.0F);
			serverPlayerEntity.updatePosition(spawnBlockPos.getX() + 0.5F, spawnBlockPos.getY(), spawnBlockPos.getZ() + 0.5F);
			return true;
		}
		return false;
	}
	
	public static boolean isNewPlayer(ServerPlayerEntity serverPlayerEntity) {
		return serverPlayerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)) == 0
			&& serverPlayerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)) == 0;
	}
	
}
