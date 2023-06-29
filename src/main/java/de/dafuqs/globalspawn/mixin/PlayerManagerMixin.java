package de.dafuqs.globalspawn.mixin;

import com.llamalad7.mixinextras.injector.*;
import de.dafuqs.globalspawn.*;
import net.minecraft.nbt.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	
	@Shadow @Final private MinecraftServer server;
	
	/**
	 * Called everytime a player connects to the server,
	 * and its profile is being loaded from disk
	 * => Change the players position as early as possible
	 */
	@ModifyReturnValue(method = "loadPlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
	public NbtCompound loadPlayerData(NbtCompound original, ServerPlayerEntity player) {
		if (GlobalSpawnManager.isInitialSpawnPointActive(this.server) && GlobalSpawnMixinHandler.isNewPlayer(player)) {
			original = GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForNewPlayer(this.server, original);
			player.readNbt(original);
		} else if (GlobalSpawnManager.isGlobalSpawnPointActive(this.server) && GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.alwaysSpawnAtGlobalSpawnOnJoin) {
			original = GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForExistingPlayer(this.server, original);
			player.readNbt(original);
		}
		return original;
	}
	
}