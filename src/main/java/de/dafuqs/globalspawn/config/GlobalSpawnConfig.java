package de.dafuqs.globalspawn.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;

@Config(name = "GlobalSpawn")
public class GlobalSpawnConfig implements ConfigData {

    public boolean active = false;
    public String spawnDimension = "minecraft:overworld";
    public int spawnX = 50;
    public int spawnY = 80;
    public int spawnZ = 50;

}
