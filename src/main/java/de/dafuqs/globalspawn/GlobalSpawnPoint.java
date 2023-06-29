package de.dafuqs.globalspawn;

import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

public class GlobalSpawnPoint {
	
	private final RegistryKey<World> dimension;
	private final BlockPos position;
	private final float angle;
	
	public GlobalSpawnPoint(RegistryKey<World> spawnPointDimension, BlockPos position, float angle) {
		this.dimension = spawnPointDimension;
		this.position = position;
		this.angle = angle;
	}
	
	public NbtCompound getSpawnNbtCompound(@Nullable NbtCompound nbtCompound) {
		if (nbtCompound == null) {
			nbtCompound = new NbtCompound();
		}
		
		nbtCompound.putString("Dimension", dimension.getValue().toString());
		nbtCompound.putFloat("SpawnAngle", angle);
		
		NbtList listTag = new NbtList();
		listTag.addElement(0, NbtDouble.of(position.getX() + 0.5));
		listTag.addElement(1, NbtDouble.of(position.getY()));
		listTag.addElement(2, NbtDouble.of(position.getZ() + 0.5));
		nbtCompound.put("Pos", listTag);
		
		return nbtCompound;
	}
	
	public RegistryKey<World> getDimension() {
		return dimension;
	}
	
	public BlockPos getPos() {
		return position;
	}
	
	public float getAngle() {
		return angle;
	}
	
}