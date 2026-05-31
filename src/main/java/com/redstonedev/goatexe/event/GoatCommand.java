package com.redstonedev.goatexe.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 *   /goat run torch_redstone   - turns all torches near the player into redstone torches
 *   /goat run torch_disappear  - makes all torches near the player disappear
 */
public class GoatCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("goat")
                        .requires(src -> src.hasPermission(0))
                        .then(Commands.literal("run")
                                .then(Commands.literal("torch_redstone")
                                        .executes(ctx -> run(ctx, true)))
                                .then(Commands.literal("torch_disappear")
                                        .executes(ctx -> run(ctx, false)))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, boolean toRedstone) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        ServerLevel level = source.getLevel();
        int changed = toRedstone
                ? TorchCorruptor.toRedstone(level, player.blockPosition())
                : TorchCorruptor.disappear(level, player.blockPosition());
        source.sendSuccess(Component.literal(
                toRedstone ? ("Turned " + changed + " torch(es) to redstone.")
                           : (changed + " torch(es) vanished.")), false);
        return changed;
    }
}
