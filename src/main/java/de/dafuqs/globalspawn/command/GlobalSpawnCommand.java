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

public class GlobalSpawnCommand {
	
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("globalspawnpoint")
				.requires((source) -> source.hasPermissionLevel(GlobalSpawnCommon.GLOBAL_SPAWN_CONFIG.commandPermissionLevel))
				.executes((commandContext) -> {
					return GlobalSpawnCommand.executeQuery(commandContext.getSource());
				})
				.then(CommandManager.literal("query").executes((commandContext) -> {
					return GlobalSpawnCommand.executeQuery(commandContext.getSource());
				})).then(CommandManager.literal("unset").executes((commandContext) -> {
					return GlobalSpawnCommand.executeUnset(commandContext.getSource());
				})).then(CommandManager.literal("set").executes((commandContext) -> {
					return GlobalSpawnCommand.executeSet(commandContext.getSource(), commandContext.getSource().getWorld(), BlockPos.ofFloored((commandContext.getSource()).getPosition()), commandContext.getSource().getRotation().y);
				}))));
	}
	
	static int executeQuery(ServerCommandSource source) {
		GlobalSpawnPoint globalSpawnPoint = GlobalSpawnManager.getGlobalRespawnPoint();
		if (globalSpawnPoint == null) {
			source.sendFeedback(() -> Text.translatable("commands.globalspawn.globalspawnpoint.query_not_set"), false);
		} else {
			BlockPos spawnBlockPos = globalSpawnPoint.getPos();
			RegistryKey<World> spawnWorld = globalSpawnPoint.getDimension();
			float angle = globalSpawnPoint.getAngle();
			
			source.sendFeedback(() -> Text.translatable("commands.globalspawn.globalspawnpoint.query_set_at", spawnWorld.getValue(), spawnBlockPos.getX(), spawnBlockPos.getY(), spawnBlockPos.getZ(), angle), false);
		}
		return 1;
	}
	
	static int executeSet(ServerCommandSource source, ServerWorld serverWorld, BlockPos blockPos, float angle) {
		GlobalSpawnPoint globalSpawnPoint = new GlobalSpawnPoint(serverWorld.getRegistryKey(), blockPos, angle);
		GlobalSpawnManager.setGlobalSpawnPoint(globalSpawnPoint);
		source.sendFeedback(() -> Text.translatable("commands.globalspawn.globalspawnpoint.set_to", serverWorld.getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), angle), true);
		return 1;
	}
	
	static int executeUnset(ServerCommandSource source) {
		GlobalSpawnManager.unsetGlobalSpawnPoint();
		source.sendFeedback(() -> Text.translatable("commands.globalspawn.globalspawnpoint.unset"), true);
		return 1;
	}
	
}