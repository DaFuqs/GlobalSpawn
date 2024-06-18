package de.dafuqs.globalspawn;

import net.minecraft.nbt.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.stat.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

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
			return GlobalSpawnManager.getInitialSpawnPoint().getSpawnNbtCompound(server, nbtCompound);
		} else if (GlobalSpawnManager.isGlobalSpawnPointActive(server)) {
			return GlobalSpawnManager.getGlobalRespawnPoint().getSpawnNbtCompound(server, nbtCompound);
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
			return GlobalSpawnManager.getGlobalRespawnPoint().getSpawnNbtCompound(server, nbtCompound);
		} else {
			return nbtCompound;
		}
	}
	
	public static boolean isNewPlayer(ServerPlayerEntity serverPlayerEntity) {
		return serverPlayerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)) == 0
			&& serverPlayerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)) == 0;
	}
	
}
