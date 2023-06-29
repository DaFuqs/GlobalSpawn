package de.dafuqs.globalspawn.mixin;

import com.mojang.authlib.*;
import de.dafuqs.globalspawn.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.server.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
	
	@Shadow @Final public MinecraftServer server;
	
	@Shadow private RegistryKey<World> spawnPointDimension;
	
	@Shadow private boolean spawnForced;
	
	@Shadow private float spawnAngle;
	
	@Shadow @Nullable private BlockPos spawnPointPosition;
	
	@Shadow public abstract void readCustomDataFromNbt(NbtCompound nbt);
	
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}
	
	@Inject(method = "getSpawnPointDimension()Lnet/minecraft/registry/RegistryKey;", at = @At("HEAD"), cancellable = true)
	public void globalspawn$getSpawnPointDimension(CallbackInfoReturnable<RegistryKey<World>> cir) {
		if(globalspawn$shouldOverrideRespawn()) {
			cir.setReturnValue(GlobalSpawnManager.getGlobalRespawnPoint().getDimension());
		} else {
			cir.setReturnValue(this.spawnPointDimension);
		}
	}
	
	@Inject(method = "getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;", at = @At("HEAD"), cancellable = true)
	public void globalspawn$getSpawnPointPosition(CallbackInfoReturnable<BlockPos> cir) {
		if(globalspawn$shouldOverrideRespawn()) {
			cir.setReturnValue(GlobalSpawnManager.getGlobalRespawnPoint().getPos());
		} else {
			cir.setReturnValue(this.spawnPointPosition);
		}
	}
	
	@Inject(method = "getSpawnAngle()F", at = @At("HEAD"), cancellable = true)
	public void globalspawn$getSpawnAngle(CallbackInfoReturnable<Float> cir) {
		if(globalspawn$shouldOverrideRespawn()) {
			cir.setReturnValue(GlobalSpawnManager.getGlobalRespawnPoint().getAngle());
		} else {
			cir.setReturnValue(this.spawnAngle);
		}
	}
	
	
	@Inject(method = "isSpawnForced()Z", at = @At("HEAD"), cancellable = true)
	public void globalspawn$isSpawnForced(CallbackInfoReturnable<Boolean> cir) {
		if(globalspawn$shouldOverrideRespawn()) {
			cir.setReturnValue(true);
		} else {
			cir.setReturnValue(this.spawnForced);
		}
	}
	
	/**
	 * Called on spawn of a player
	 * @param world        The default world
	 * @param callbackInfo CallbackInfo
	 */
	@Inject(method = "moveToSpawn", at = @At("HEAD"), cancellable = true)
	private void globalspawn$moveToSpawn(ServerWorld world, CallbackInfo callbackInfo) {
		if (GlobalSpawnMixinHandler.movePlayerToSpawn((ServerPlayerEntity) (Object) this)) {
			callbackInfo.cancel();
		}
	}
	
	private boolean globalspawn$shouldOverrideRespawn() {
		 if(!GlobalSpawnManager.isGlobalSpawnPointActive(this.server)) {
			 return false;
		 }
			
		BlockPos blockPos = this.spawnPointPosition;
		ServerWorld serverWorld = this.server.getWorld(spawnPointDimension);
		if (serverWorld != null && blockPos != null) {
			return PlayerEntity.findRespawnPosition(serverWorld, blockPos, this.spawnAngle, this.spawnForced, true).isEmpty();
		} else {
			return true;
		}
	}
	
}
