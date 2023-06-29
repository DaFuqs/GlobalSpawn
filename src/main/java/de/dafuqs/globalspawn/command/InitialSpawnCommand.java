package de.dafuqs.globalspawn.command;

import de.dafuqs.globalspawn.GlobalSpawnCommon;
import de.dafuqs.globalspawn.GlobalSpawnManager;
import de.dafuqs.globalspawn.GlobalSpawnPoint;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InitialSpawnCommand {
	
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("initialspawnpoint")
				.requires((source) -> source.hasPermissionLevel(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.commandPermissionLevel))
				.executes((commandContext) -> {
					return InitialSpawnCommand.executeQuery(commandContext.getSource());
				})
				.then(CommandManager.literal("query").executes((commandContext) -> {
					return InitialSpawnCommand.executeQuery(commandContext.getSource());
				})).then(CommandManager.literal("unset").executes((commandContext) -> {
					return InitialSpawnCommand.executeUnset(commandContext.getSource());
				})).then(CommandManager.literal("set").executes((commandContext) -> {
					return InitialSpawnCommand.executeSet(commandContext.getSource(), commandContext.getSource().getWorld(), BlockPos.ofFloored((commandContext.getSource()).getPosition()), commandContext.getSource().getRotation().y);
				}))));
	}
	
	static int executeQuery(ServerCommandSource source) {
		GlobalSpawnPoint initialSpawnPoint = GlobalSpawnManager.getInitialSpawnPoint();
		if (initialSpawnPoint == null) {
			source.sendFeedback(() -> Text.translatable("commands.globalspawn.initialspawnpoint.query_not_set"), false);
		} else {
			BlockPos spawnBlockPos = initialSpawnPoint.getPos();
			RegistryKey<World> spawnWorld = initialSpawnPoint.getDimension();
			float angle = initialSpawnPoint.getAngle();
			
			source.sendFeedback(() -> Text.translatable("commands.globalspawn.initialspawnpoint.query_set_at", spawnWorld.getValue(), spawnBlockPos.getX(), spawnBlockPos.getY(), spawnBlockPos.getZ(), angle), false);
		}
		return 1;
	}
	
	static int executeSet(ServerCommandSource source, ServerWorld serverWorld, BlockPos blockPos, float angle) {
		GlobalSpawnPoint initialSpawnPoint = new GlobalSpawnPoint(serverWorld.getRegistryKey(), blockPos, angle);
		GlobalSpawnManager.setInitialSpawnPoint(initialSpawnPoint);
		source.sendFeedback(() -> Text.translatable("commands.globalspawn.initialspawnpoint.set_to", serverWorld.getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), angle), true);
		return 1;
	}
	
	static int executeUnset(ServerCommandSource source) {
		GlobalSpawnManager.unsetInitialSpawnPoint();
		source.sendFeedback(() -> Text.translatable("commands.globalspawn.initialspawnpoint.unset"), true);
		return 1;
	}
	
}