package de.dafuqs.globalspawn.mixin;

import de.dafuqs.globalspawn.GlobalSpawnCommon;
import de.dafuqs.globalspawn.GlobalSpawnManager;
import de.dafuqs.globalspawn.GlobalSpawnMixinHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    @Final
    private Map<UUID, ServerPlayerEntity> playerMap;

    /**
     * Called everytime a player connects to the server,
     * and its profile is being loaded from disk
     * => Change the players position as early as possible
     */
    @Inject(method = "loadPlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"), cancellable = true)
    public void loadPlayerData(ServerPlayerEntity player, @NotNull CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound currentCompound = cir.getReturnValue();
        if(GlobalSpawnManager.isInitialSpawnPointActive() && GlobalSpawnMixinHandler.isNewPlayer(currentCompound)) {
            currentCompound = new NbtCompound();
            GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForNewPlayer(currentCompound);
            player.readNbt(currentCompound);
            cir.setReturnValue(currentCompound);
        } else if(GlobalSpawnManager.isGlobalRespawnPointActive() && GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.alwaysSpawnAtGlobalSpawnOnJoin) {
            currentCompound = GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForExistingPlayer(currentCompound);
            player.readNbt(currentCompound);
            cir.setReturnValue(currentCompound);
        }
    }

    @Inject(method = "respawnPlayer", at = @At("HEAD"), cancellable = true)
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> callbackInfoReturnable) {
        if(GlobalSpawnManager.isGlobalRespawnPointActive()) {
            BlockPos spawnPointBlockPos = player.getSpawnPointPosition();
            float spawnAngle = player.getSpawnAngle();
            RegistryKey<World> originalServerWorldRegistryKey = player.getSpawnPointDimension();
            ServerWorld originalSpawnPoint = this.server.getWorld(originalServerWorldRegistryKey);
            Optional<Vec3d> originalSpawnPosition;
            if (spawnPointBlockPos != null) {
                originalSpawnPosition = PlayerEntity.findRespawnPosition(originalSpawnPoint, spawnPointBlockPos, spawnAngle, true, true);
            } else {
                originalSpawnPosition = Optional.empty();
            }

            // Override vanilla respawning behavior if:
            // no respawn position
            // or spawn position is obstructed (respawn anchor empty, bed destroyed, ...)
            boolean shouldOverrideVanilla = originalSpawnPosition.isEmpty();
            if (shouldOverrideVanilla) {
                this.players.remove(player);
                player.getWorld().removePlayer(player, Entity.RemovalReason.DISCARDED);

                ServerWorld overriddenSpawnPointWorld = this.server.getWorld(GlobalSpawnManager.getGlobalRespawnPoint().getSpawnDimension());

                ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, overriddenSpawnPointWorld, player.getGameProfile());
                serverPlayerEntity.networkHandler = player.networkHandler;
                serverPlayerEntity.copyFrom(player, alive);
                serverPlayerEntity.setId(player.getId());
                serverPlayerEntity.setMainArm(player.getMainArm());

                for (String scoreBoardTag : player.getScoreboardTags()) {
                    serverPlayerEntity.addScoreboardTag(scoreBoardTag);
                }

                while (!overriddenSpawnPointWorld.isSpaceEmpty(serverPlayerEntity) && serverPlayerEntity.getY() < (double) overriddenSpawnPointWorld.getTopY()) {
                    serverPlayerEntity.setPosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0D, serverPlayerEntity.getZ());
                }

                WorldProperties worldProperties = serverPlayerEntity.world.getLevelProperties();
                serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.world.method_40134(), serverPlayerEntity.world.getRegistryKey(), BiomeAccess.hashSeed(serverPlayerEntity.getWorld().getSeed()), serverPlayerEntity.interactionManager.getGameMode(), serverPlayerEntity.interactionManager.getPreviousGameMode(), serverPlayerEntity.getWorld().isDebugWorld(), serverPlayerEntity.getWorld().isFlat(), alive));
                serverPlayerEntity.networkHandler.requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
                serverPlayerEntity.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(overriddenSpawnPointWorld.getSpawnPos(), overriddenSpawnPointWorld.getSpawnAngle()));
                serverPlayerEntity.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
                serverPlayerEntity.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(serverPlayerEntity.experienceProgress, serverPlayerEntity.totalExperience, serverPlayerEntity.experienceLevel));

                ((PlayerManager) (Object) this).sendWorldInfo(serverPlayerEntity, overriddenSpawnPointWorld);
                ((PlayerManager) (Object) this).sendCommandTree(serverPlayerEntity);

                overriddenSpawnPointWorld.onPlayerRespawned(serverPlayerEntity);
                this.players.add(serverPlayerEntity);
                this.playerMap.put(serverPlayerEntity.getUuid(), serverPlayerEntity);
                serverPlayerEntity.onSpawn();
                serverPlayerEntity.setHealth(serverPlayerEntity.getHealth());

                callbackInfoReturnable.setReturnValue(serverPlayerEntity);
            }
        }
    }

}