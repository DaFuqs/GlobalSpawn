package de.dafuqs.globalspawn.mixin;

import de.dafuqs.globalspawn.GlobalSpawnMixinHandler;
import de.dafuqs.globalspawn.GlobalSpawnPoint;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
	
	@Shadow
	private RegistryKey<World> spawnPointDimension;
	@Shadow
	private BlockPos spawnPointPosition;
	
	/**
	 * Override for ServerPlayerEntities "getSpawnPointDimension"
	 *
	 * @param cir The modified spawn point dimension
	 */
	@Inject(method = "getSpawnPointDimension", at = @At("HEAD"), cancellable = true)
	private void getSpawnPointDimension(CallbackInfoReturnable<RegistryKey<World>> cir) {
		GlobalSpawnPoint globalSpawnPoint = GlobalSpawnMixinHandler.setRespawningPlayersDataWithoutSpawnPoint(spawnPointDimension, spawnPointPosition, spawnPointPosition != null);
		if (globalSpawnPoint != null) {
			cir.setReturnValue(globalSpawnPoint.getSpawnDimension());
		}
	}
	
	/**
	 * Override for ServerPlayerEntities "getSpawnPointPosition"
	 *
	 * @param cir The modified spawn point position
	 */
	@Inject(method = "getSpawnPointPosition", at = @At("HEAD"), cancellable = true)
	public void getSpawnPointPosition(CallbackInfoReturnable<BlockPos> cir) {
		GlobalSpawnPoint globalSpawnPoint = GlobalSpawnMixinHandler.setRespawningPlayersDataWithoutSpawnPoint(spawnPointDimension, spawnPointPosition, spawnPointPosition != null);
		if (globalSpawnPoint != null) {
			cir.setReturnValue(globalSpawnPoint.getSpawnBlockPos());
		}
	}
	
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
			((ServerPlayerEntity) (Object) this).refreshPositionAndAngles(((ServerPlayerEntity) (Object) this).getBlockPos(), 0.0F, 0.0F);
			callbackInfo.cancel();
		}
	}
	
}
