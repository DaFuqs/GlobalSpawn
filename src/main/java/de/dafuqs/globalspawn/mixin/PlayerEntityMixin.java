package de.dafuqs.globalspawn.mixin;

import de.dafuqs.globalspawn.GlobalSpawnMixinHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    /**
     * Called when a player makes it's first connection ever
     * @param x
     * @return
     */
    @ModifyVariable(method = "onPlayerConnect", at = @At("STORE"), ordinal = 0)
    private CompoundTag onPlayerConnect(CompoundTag x) {
        return GlobalSpawnMixinHandler.modifySpawnRegistry(x);
    }

    /**
     * Called when pressing "respawn" after death
     * Searching for bed, respawn anchor, ... at respawn point position
     * @param optional
     * @return
     */
    @ModifyVariable(method = "respawnPlayer", at = @At("STORE"), ordinal = 0)
    public Optional<Vec3d> respawnPlayer(Optional<Vec3d> optional) {
        return GlobalSpawnMixinHandler.getRespawnPlayer(optional);
    }

}