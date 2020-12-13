package de.dafuqs.globalspawn.mixin;

import de.dafuqs.globalspawn.GlobalSpawnManager;
import de.dafuqs.globalspawn.GlobalSpawnMixinHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.DemoServerPlayerInteractionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

    @Shadow
    private GameMode gameMode;

    /**
     * Called everytime a player connects to the server
     * Weather it's the first connection or not
     * @param x When new player: An empty NBTag
     * @return modified NBTag with globalSpawns spawn properties
     */
    @ModifyVariable(method = "onPlayerConnect", at = @At("STORE"), ordinal = 0)
    private CompoundTag onPlayerConnect(CompoundTag x) {
        return GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForNewPlayer(x);
    }

    @Inject(method = "respawnPlayer", at = @At("HEAD"), cancellable = true)
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> callbackInfoReturnable) {

        BlockPos blockPos = player.getSpawnPointPosition();
        float f = player.getSpawnAngle();
        boolean bl = player.isSpawnPointSet();
        RegistryKey<World> serverWorldRegistryKey = player.getSpawnPointDimension();
        ServerWorld originalSpawnPoint = this.server.getWorld(serverWorldRegistryKey);
        Optional<Vec3d> originalSpawnPosition = PlayerEntity.findRespawnPosition(originalSpawnPoint, blockPos, f, bl, true);

        // Override vanilla respawning behavior if:
        // no respawn position
        // or spawn position is obstructed (respawn anchor empty, bed destroyed, ...)
        boolean shouldOverrideVanilla = GlobalSpawnManager.isActive() && !originalSpawnPosition.isPresent();
        if(shouldOverrideVanilla) {
            this.players.remove(player);
            player.getServerWorld().removePlayer(player);

            ServerWorld overriddenSpawnPointWorld = this.server.getWorld(GlobalSpawnManager.getGlobalSpawnPoint().getSpawnDimension());

            Object serverPlayerInteractionManager2;
            if (this.server.isDemo()) {
                serverPlayerInteractionManager2 = new DemoServerPlayerInteractionManager(overriddenSpawnPointWorld);
            } else {
                serverPlayerInteractionManager2 = new ServerPlayerInteractionManager(overriddenSpawnPointWorld);
            }

            ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, overriddenSpawnPointWorld, player.getGameProfile(), (ServerPlayerInteractionManager)serverPlayerInteractionManager2);
            serverPlayerEntity.networkHandler = player.networkHandler;
            serverPlayerEntity.copyFrom(player, alive);
            serverPlayerEntity.setEntityId(player.getEntityId());
            serverPlayerEntity.setMainArm(player.getMainArm());

            for (String string : player.getScoreboardTags()) {
                serverPlayerEntity.addScoreboardTag(string);
            }

            this.setGameModeCustom(serverPlayerEntity, player, overriddenSpawnPointWorld);
            serverPlayerEntity.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));

            Vec3d vec3d = GlobalSpawnManager.getGlobalSpawnPoint().getSpawnVec3D();
            serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f, 0.0F);
            while(!overriddenSpawnPointWorld.isSpaceEmpty(serverPlayerEntity) && serverPlayerEntity.getY() < 256.0D) {
                serverPlayerEntity.updatePosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0D, serverPlayerEntity.getZ());
            }

            WorldProperties worldProperties = serverPlayerEntity.world.getLevelProperties();
            serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.world.getDimension(), serverPlayerEntity.world.getRegistryKey(), BiomeAccess.hashSeed(serverPlayerEntity.getServerWorld().getSeed()), serverPlayerEntity.interactionManager.getGameMode(), serverPlayerEntity.interactionManager.getPreviousGameMode(), serverPlayerEntity.getServerWorld().isDebugWorld(), serverPlayerEntity.getServerWorld().isFlat(), alive));
            serverPlayerEntity.networkHandler.requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.yaw, serverPlayerEntity.pitch);
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

    private void setGameModeCustom(ServerPlayerEntity player, @Nullable ServerPlayerEntity oldPlayer, ServerWorld world) {
        if (oldPlayer != null) {
            player.interactionManager.setGameMode(oldPlayer.interactionManager.getGameMode(), oldPlayer.interactionManager.getPreviousGameMode());
        } else if (this.gameMode != null) {
            player.interactionManager.setGameMode(this.gameMode, GameMode.NOT_SET);
        }

        player.interactionManager.setGameModeIfNotPresent(world.getServer().getSaveProperties().getGameMode());
    }

}