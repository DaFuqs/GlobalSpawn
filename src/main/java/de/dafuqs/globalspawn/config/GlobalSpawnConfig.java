package de.dafuqs.globalspawn.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "GlobalSpawn")
public class GlobalSpawnConfig implements ConfigData {

    public int commandPermissionLevel = 2;
    public boolean alwaysSpawnAtGlobalSpawnOnJoin = false;

    public boolean globalRespawnPointActive = false;
    public String globalRespawnDimension = "minecraft:overworld";
    public int globalRespawnPositionX = 50;
    public int globalRespawnPositionY = 80;
    public int globalRespawnPositionZ = 50;

    public boolean initialSpawnPointActive = false;
    public String initialSpawnPointDimension = "minecraft:overworld";
    public int initialSpawnPositionX = 50;
    public int initialSpawnPositionY = 80;
    public int initialSpawnPositionZ = 50;
}
