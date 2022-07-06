package de.dafuqs.globalspawn.mixin;

import de.dafuqs.globalspawn.GlobalSpawnMixinHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
	
	/**
	 * Called on spawn of a player
	 *
	 * @param world        The default world
	 * @param callbackInfo CallbackInfo
	 */
	@Inject(method = "moveToSpawn", at = @At("HEAD"), cancellable = true)
	private void moveToSpawn(ServerWorld world, CallbackInfo callbackInfo) {
		boolean set = GlobalSpawnMixinHandler.movePlayerToSpawn((ServerPlayerEntity) (Object) this);
		if (set) {
			callbackInfo.cancel();
		}
	}
	
}
