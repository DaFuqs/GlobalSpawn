package de.dafuqs.globalspawn;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class GlobalSpawnPoint {

    private final RegistryKey<World> spawnPointDimension;
    private final BlockPos spawnPointPosition;

    public GlobalSpawnPoint(RegistryKey<World> spawnPointDimension, BlockPos spawnPointPosition) {
        this.spawnPointDimension = spawnPointDimension;
        this.spawnPointPosition = spawnPointPosition;
    }

    public NbtCompound getSpawnNbtCompound(NbtCompound nbtCompound) {
        if(nbtCompound == null) {
            nbtCompound = new NbtCompound();
        }

        nbtCompound.putString("Dimension", spawnPointDimension.getValue().toString());

        NbtList listTag = new NbtList();
        listTag.addElement(0, NbtDouble.of(spawnPointPosition.getX() + 0.5));
        listTag.addElement(1, NbtDouble.of(spawnPointPosition.getY()));
        listTag.addElement(2, NbtDouble.of(spawnPointPosition.getZ() + 0.5));

        nbtCompound.put("Pos", listTag);
        return nbtCompound;
    }

    public RegistryKey<World> getSpawnDimension() {
        return spawnPointDimension;
    }

    public Vec3d getSpawnVec3D() {
        return new Vec3d(spawnPointPosition.getX() + 0.5, spawnPointPosition.getY(), spawnPointPosition.getZ() + 0.5);
    }

    public BlockPos getSpawnBlockPos() {
        return new BlockPos(spawnPointPosition.getX(), spawnPointPosition.getY(), spawnPointPosition.getZ());
    }

}