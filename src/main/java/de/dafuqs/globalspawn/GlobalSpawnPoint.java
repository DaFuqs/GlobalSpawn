package de.dafuqs.globalspawn;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class GlobalSpawnPoint {

    public RegistryKey<World> spawnPointDimension;
    public BlockPos spawnPointPosition;


    public GlobalSpawnPoint(RegistryKey<World> spawnPointDimension, BlockPos spawnPointPosition) {
        this.spawnPointDimension = spawnPointDimension;
        this.spawnPointPosition = spawnPointPosition;
    }

    public CompoundTag getSpawnCompoundTag() {
        CompoundTag compoundTag1 = new CompoundTag();
        compoundTag1.putString("Dimension", spawnPointDimension.getValue().toString());

        ListTag listTag = new ListTag();
        listTag.addTag(0, DoubleTag.of(spawnPointPosition.getX()));
        listTag.addTag(1, DoubleTag.of(spawnPointPosition.getY()));
        listTag.addTag(2, DoubleTag.of(spawnPointPosition.getZ()));

        compoundTag1.put("Pos", listTag);
        return compoundTag1;
    }

    public Vec3d getSpawnVec3D() {
        return new Vec3d(spawnPointPosition.getX(), spawnPointPosition.getY(), spawnPointPosition.getZ());
    }

    public BlockPos getSpawnBlockPos() {
        return new BlockPos(spawnPointPosition.getX(), spawnPointPosition.getY(), spawnPointPosition.getZ());
    }

}