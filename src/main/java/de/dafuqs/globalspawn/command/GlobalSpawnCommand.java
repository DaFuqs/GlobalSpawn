package de.dafuqs.globalspawn.command;

import de.dafuqs.globalspawn.GlobalSpawnManager;
import de.dafuqs.globalspawn.GlobalSpawnPoint;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class GlobalSpawnCommand {

    enum Action {
        QUERY,
        SET,
        UNSET
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("globalspawnpoint")
                    .requires((source) -> source.hasPermissionLevel(0))
                    .executes((commandContext) -> {
                        return GlobalSpawnCommand.executeGlobalRespawnPoint(commandContext.getSource(), GlobalSpawnCommand.Action.QUERY, null, null);
                    })
                    .then(CommandManager.literal("query").executes((commandContext) -> {
                        return GlobalSpawnCommand.executeGlobalRespawnPoint(commandContext.getSource(), GlobalSpawnCommand.Action.QUERY, null, null);
                    })).then(CommandManager.literal("unset").executes((commandContext) -> {
                        return GlobalSpawnCommand.executeGlobalRespawnPoint(commandContext.getSource(), GlobalSpawnCommand.Action.UNSET, null, null);
                    })).then(CommandManager.literal("set").executes((commandContext) -> {
                        return GlobalSpawnCommand.executeGlobalRespawnPoint(commandContext.getSource(), GlobalSpawnCommand.Action.SET, commandContext.getSource().getWorld(), new BlockPos((commandContext.getSource()).getPosition()));
                    }))
            );
        });
    }

    static int executeGlobalRespawnPoint(ServerCommandSource source, Action action, ServerWorld serverWorld, BlockPos blockPos) {
        GlobalSpawnPoint globalSpawnPoint;
        switch (action) {
            case QUERY -> {
                globalSpawnPoint = GlobalSpawnManager.getGlobalRespawnPoint();
                if (globalSpawnPoint == null) {
                    source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.query_not_set"), false);
                } else {
                    BlockPos spawnBlockPos = globalSpawnPoint.getSpawnBlockPos();
                    RegistryKey<World> spawnWorld = globalSpawnPoint.getSpawnDimension();

                    source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.query_set_at", spawnWorld.getValue(), spawnBlockPos.getX(), spawnBlockPos.getY(), spawnBlockPos.getZ()), false);
                }
            }
            case SET -> {
                globalSpawnPoint = new GlobalSpawnPoint(serverWorld.getRegistryKey(), blockPos);
                GlobalSpawnManager.setRespawnPoint(globalSpawnPoint);
                source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.set_to", serverWorld.getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
            }
            case UNSET -> {
                GlobalSpawnManager.unsetRespawnPoint();
                source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.unset"), true);
            }
        }
        return 1;
    }

}