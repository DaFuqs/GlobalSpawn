package de.dafuqs.globalspawn.mixin;

import de.dafuqs.globalspawn.GlobalSpawnMixinHandler;
import de.dafuqs.globalspawn.GlobalSpawnPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
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
    @Shadow
    private boolean spawnPointSet;
    @Shadow
    @Final
    public MinecraftServer server;

    @Inject(method = "getSpawnPointDimension", at = @At("HEAD"), cancellable = true)
    private void getSpawnPointDimension(CallbackInfoReturnable<RegistryKey<World>> cir) {
        GlobalSpawnPoint globalSpawnPoint = GlobalSpawnMixinHandler.getRespawnData(spawnPointDimension, spawnPointPosition, spawnPointSet);
        if(globalSpawnPoint != null) {
            spawnPointDimension = globalSpawnPoint.spawnPointDimension;
            spawnPointPosition = globalSpawnPoint.spawnPointPosition;
            cir.setReturnValue(globalSpawnPoint.spawnPointDimension);
        }
    }

    @Inject(method = "getSpawnPointPosition", at = @At("HEAD"), cancellable = true)
    public void getSpawnPointPosition(CallbackInfoReturnable<BlockPos> cir) {
        GlobalSpawnPoint globalSpawnPoint = GlobalSpawnMixinHandler.getRespawnData(spawnPointDimension, spawnPointPosition, spawnPointSet);
        if(globalSpawnPoint != null) {
            spawnPointDimension = globalSpawnPoint.spawnPointDimension;
            spawnPointPosition = globalSpawnPoint.spawnPointPosition;
            cir.setReturnValue(globalSpawnPoint.spawnPointPosition);
        }
    }

    // on first connect
    @Inject(method = "moveToSpawn", at = @At("HEAD"), cancellable = true)
    private void moveToSpawn(ServerWorld world, CallbackInfo ci) {
        boolean set = GlobalSpawnMixinHandler.moveToSpawn((ServerPlayerEntity) (Object) this);
        if(set) {
            ci.cancel();
        }
    }

}
