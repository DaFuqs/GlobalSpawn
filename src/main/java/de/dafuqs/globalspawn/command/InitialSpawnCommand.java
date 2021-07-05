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

public class InitialSpawnCommand {

    enum Action {
        QUERY,
        SET,
        UNSET
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("initialspawnpoint")
                    .requires((source) -> source.hasPermissionLevel(0))
                    .executes((commandContext) -> {
                        return InitialSpawnCommand.executeInitialSpawnPoint(commandContext.getSource(), Action.QUERY, null, null);
                    })
                    .then(CommandManager.literal("query").executes((commandContext) -> {
                        return InitialSpawnCommand.executeInitialSpawnPoint(commandContext.getSource(), Action.QUERY, null, null);
                    })).then(CommandManager.literal("unset").executes((commandContext) -> {
                        return InitialSpawnCommand.executeInitialSpawnPoint(commandContext.getSource(), Action.UNSET, null, null);
                    })).then(CommandManager.literal("set").executes((commandContext) -> {
                        return InitialSpawnCommand.executeInitialSpawnPoint(commandContext.getSource(), Action.SET, commandContext.getSource().getWorld(), new BlockPos((commandContext.getSource()).getPosition()));
                    }))
            );
        });
    }

    static int executeInitialSpawnPoint(ServerCommandSource source, Action action, ServerWorld serverWorld, BlockPos blockPos) {
        GlobalSpawnPoint initialSpawnPoint;
        switch (action) {
            case QUERY -> {
                initialSpawnPoint = GlobalSpawnManager.getInitialSpawnPoint();
                if (initialSpawnPoint == null) {
                    source.sendFeedback(new TranslatableText("commands.globalspawn.initialspawnpoint.query_not_set"), false);
                } else {
                    BlockPos spawnBlockPos = initialSpawnPoint.getSpawnBlockPos();
                    RegistryKey<World> spawnWorld = initialSpawnPoint.getSpawnDimension();

                    source.sendFeedback(new TranslatableText("commands.globalspawn.initialspawnpoint.query_set_at", spawnWorld.getValue(), spawnBlockPos.getX(), spawnBlockPos.getY(), spawnBlockPos.getZ()), false);
                }
            }
            case SET -> {
                initialSpawnPoint = new GlobalSpawnPoint(serverWorld.getRegistryKey(), blockPos);
                GlobalSpawnManager.setInitialSpawnPoint(initialSpawnPoint);
                source.sendFeedback(new TranslatableText("commands.globalspawn.initialspawnpoint.set_to", serverWorld.getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
            }
            case UNSET -> {
                GlobalSpawnManager.unsetInitialSpawnPoint();
                source.sendFeedback(new TranslatableText("commands.globalspawn.initialspawnpoint.unset"), true);
            }
        }
        return 1;
    }

}