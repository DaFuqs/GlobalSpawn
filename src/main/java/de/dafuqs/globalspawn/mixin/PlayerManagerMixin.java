package de.dafuqs.globalspawn.mixin;

import com.llamalad7.mixinextras.injector.*;
import de.dafuqs.globalspawn.*;
import net.minecraft.nbt.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	
	@Shadow @Final private MinecraftServer server;
	
	/**
	 * Called everytime a player connects to the server,
	 * and its profile is being loaded from disk
	 * => Change the players position as early as possible
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ModifyReturnValue(method = "loadPlayerData", at = @At("RETURN"))
	public Optional<NbtCompound> loadPlayerData(Optional<NbtCompound> original, ServerPlayerEntity player) {
		NbtCompound nbt = original.orElse(new NbtCompound());
		if (GlobalSpawnManager.isInitialSpawnPointActive(this.server) && GlobalSpawnMixinHandler.isNewPlayer(player)) {
			nbt = GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForNewPlayer(this.server, nbt);
			player.readNbt(nbt);
		} else if (GlobalSpawnManager.isGlobalSpawnPointActive(this.server) && GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.alwaysSpawnAtGlobalSpawnOnJoin) {
			nbt = GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForExistingPlayer(this.server, nbt);
			player.readNbt(nbt);
		}
		return Optional.of(nbt);
	}
	
}