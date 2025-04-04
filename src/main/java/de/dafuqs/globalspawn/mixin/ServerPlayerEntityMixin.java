package de.dafuqs.globalspawn.mixin;

import com.llamalad7.mixinextras.sugar.*;
import com.mojang.authlib.*;
import de.dafuqs.globalspawn.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
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
	
	@Shadow public abstract void readCustomDataFromNbt(NbtCompound nbt);
	
	@Shadow
	@Nullable
	private ServerPlayerEntity.Respawn respawn;
	
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}
	
	@Inject(method = "getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/TeleportTarget;missingSpawnBlock(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"), cancellable = true)
	public void globalSpawn$getObstructedRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
		if(globalspawn$shouldOverrideRespawn()) {
			GlobalSpawnPoint p = GlobalSpawnManager.getGlobalRespawnPoint();
			cir.setReturnValue(new TeleportTarget(server.getWorld(p.getDimension()), Vec3d.ofCenter(p.getFinalSpawnPos(server)), Vec3d.ZERO, p.getAngle(), 0.0F, postDimensionTransition));
		}
	}
	
	@Inject(method = "getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;", shift = At.Shift.AFTER), cancellable = true)
	public void globalSpawn$getUnsetRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir, @Local ServerPlayerEntity.Respawn respawn) {
		if (respawn == null && globalspawn$shouldOverrideRespawn()) {
			GlobalSpawnPoint p = GlobalSpawnManager.getGlobalRespawnPoint();
			cir.setReturnValue(new TeleportTarget(server.getWorld(p.getDimension()), Vec3d.ofCenter(p.getFinalSpawnPos(server)), Vec3d.ZERO, p.getAngle(), 0.0F, postDimensionTransition));
		}
	}
	
	@Unique
	private boolean globalspawn$shouldOverrideRespawn() {
		if(!GlobalSpawnManager.isGlobalSpawnPointActive(this.server)) {
			return false;
		}
		
		@Nullable ServerPlayerEntity.Respawn respawn = this.respawn;
		if (respawn != null) {
			ServerWorld serverWorld = this.server.getWorld(respawn.dimension());
			return ServerPlayerEntity.findRespawnPosition(serverWorld, respawn, true).isEmpty();
		} else {
			return true;
		}
	}
	
}
