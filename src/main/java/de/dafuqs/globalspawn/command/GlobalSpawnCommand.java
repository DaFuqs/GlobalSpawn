package de.dafuqs.globalspawn.command;

import de.dafuqs.globalspawn.GlobalSpawnManager;
import de.dafuqs.globalspawn.GlobalSpawnPoint;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class GlobalSpawnCommand {

    enum Action {
        QUERY,
        SET,
        UNSET
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("globalspawnpoint")
                    .requires((source) -> source.hasPermissionLevel(0))
                    .executes((commandContext) -> {
                        return GlobalSpawnCommand.execute(commandContext.getSource(), GlobalSpawnCommand.Action.QUERY, null, null);
                    })
                    .then(CommandManager.literal("query").executes((commandContext) -> {
                        return GlobalSpawnCommand.execute(commandContext.getSource(), GlobalSpawnCommand.Action.QUERY, null, null);
                    })).then(CommandManager.literal("unset").executes((commandContext) -> {
                        return GlobalSpawnCommand.execute(commandContext.getSource(), GlobalSpawnCommand.Action.UNSET, null, null);
                    })).then(CommandManager.literal("set").executes((commandContext) -> {
                        return GlobalSpawnCommand.execute(commandContext.getSource(), GlobalSpawnCommand.Action.SET, commandContext.getSource().getWorld(), new BlockPos((commandContext.getSource()).getPosition()));
                    }))
            );
        });
    }

    static int execute(ServerCommandSource source, Action action, ServerWorld serverWorld, BlockPos blockPos) {
        GlobalSpawnPoint globalSpawnPoint;
        switch (action) {
            case QUERY:
                globalSpawnPoint = GlobalSpawnManager.get();
                if(globalSpawnPoint == null) {
                    source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.query_not_set"), false);
                } else {
                    source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.query_set_at", globalSpawnPoint.spawnPointDimension.getValue(), globalSpawnPoint.spawnPointPosition.getX(), globalSpawnPoint.spawnPointPosition.getY(), globalSpawnPoint.spawnPointPosition.getZ()), false);
                }
                break;
            case SET:
                globalSpawnPoint = new GlobalSpawnPoint(serverWorld.getRegistryKey(), blockPos);
                GlobalSpawnManager.set(globalSpawnPoint);
                source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.set_to", serverWorld.getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
                break;
            case UNSET:
                GlobalSpawnManager.unset();
                source.sendFeedback(new TranslatableText("commands.globalspawn.globalspawnpoint.unset"), true);
                break;
        }

        return 1;
    }


}