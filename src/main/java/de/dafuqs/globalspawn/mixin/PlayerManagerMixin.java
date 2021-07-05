package de.dafuqs.globalspawn.mixin;

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

    /**
     * Called everytime a player connects to the server
     * Weather it's the first connection or not
     * @param nbtCompound When new player: An empty NBTag
     * @return modified NBTag with globalSpawns spawn properties
     */
    @ModifyVariable(method = "onPlayerConnect", at = @At("STORE"), ordinal = 0)
    private NbtCompound onPlayerConnect(NbtCompound nbtCompound) {

        if (nbtCompound == null) { // true only for new players
            // new player => Add spawn tag)
            return GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForNewPlayer(nbtCompound);
        } else {
            return GlobalSpawnMixinHandler.modifySpawnRegistryPositionAndDimensionForExistingPlayer(nbtCompound);
        }
    }

    @Inject(method = "respawnPlayer", at = @At("HEAD"), cancellable = true)
    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> callbackInfoReturnable) {

        BlockPos myBlockPos = player.getSpawnPointPosition();
        float myF = player.getSpawnAngle();
        boolean myBl = player.isSpawnPointSet();
        RegistryKey<World> originalServerWorldRegistryKey = player.getSpawnPointDimension();
        ServerWorld originalSpawnPoint = this.server.getWorld(originalServerWorldRegistryKey);
        Optional<Vec3d> originalSpawnPosition;
        if(myBlockPos != null) {
            originalSpawnPosition = PlayerEntity.findRespawnPosition(originalSpawnPoint, myBlockPos, myF, myBl, true);
        } else {
            originalSpawnPosition = Optional.empty();
        }

        // Override vanilla respawning behavior if:
        // no respawn position
        // or spawn position is obstructed (respawn anchor empty, bed destroyed, ...)
        boolean shouldOverrideVanilla = GlobalSpawnManager.isGlobalRespawnPointActive() && originalSpawnPosition.isEmpty();
        if(shouldOverrideVanilla) {
            this.players.remove(player);
            player.getServerWorld().removePlayer(player, Entity.RemovalReason.DISCARDED);

            ServerWorld overriddenSpawnPointWorld = this.server.getWorld(GlobalSpawnManager.getGlobalRespawnPoint().getSpawnDimension());

            ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, overriddenSpawnPointWorld, player.getGameProfile());
            serverPlayerEntity.networkHandler = player.networkHandler;
            serverPlayerEntity.copyFrom(player, alive);
            serverPlayerEntity.setId(player.getId());
            serverPlayerEntity.setMainArm(player.getMainArm());

            for (String scoreBoardTag : player.getScoreboardTags()) {
                serverPlayerEntity.addScoreboardTag(scoreBoardTag);
            }

            while(!overriddenSpawnPointWorld.isSpaceEmpty(serverPlayerEntity) && serverPlayerEntity.getY() < (double)overriddenSpawnPointWorld.getTopY()) {
                serverPlayerEntity.setPosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0D, serverPlayerEntity.getZ());
            }

            WorldProperties worldProperties = serverPlayerEntity.world.getLevelProperties();
            serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.world.getDimension(), serverPlayerEntity.world.getRegistryKey(), BiomeAccess.hashSeed(serverPlayerEntity.getServerWorld().getSeed()), serverPlayerEntity.interactionManager.getGameMode(), serverPlayerEntity.interactionManager.getPreviousGameMode(), serverPlayerEntity.getServerWorld().isDebugWorld(), serverPlayerEntity.getServerWorld().isFlat(), alive));
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