package com.redstonedev.goatexe.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Shared torch effects used by both the goat and the /goat commands. */
public final class TorchCorruptor {
    private TorchCorruptor() {}

    private static final int RADIUS = 8;

    /** Turns nearby torches (floor + wall) into redstone torches. Returns how many changed. */
    public static int toRedstone(ServerLevel level, BlockPos center) {
        return apply(level, center, true);
    }

    /** Removes nearby torches (floor + wall). Returns how many removed. */
    public static int disappear(ServerLevel level, BlockPos center) {
        return apply(level, center, false);
    }

    private static int apply(ServerLevel level, BlockPos center, boolean toRedstone) {
        int count = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState bs = level.getBlockState(p);
                    Block b = bs.getBlock();
                    if (b == Blocks.TORCH) {
                        if (toRedstone) {
                            level.setBlock(p, Blocks.REDSTONE_TORCH.defaultBlockState(), 3);
                        } else {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                        count++;
                    } else if (b == Blocks.WALL_TORCH) {
                        if (toRedstone) {
                            Direction facing = bs.getValue(HorizontalDirectionalBlock.FACING);
                            level.setBlock(p, Blocks.REDSTONE_WALL_TORCH.defaultBlockState()
                                    .setValue(HorizontalDirectionalBlock.FACING, facing), 3);
                        } else {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
