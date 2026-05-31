package com.redstonedev.goatexe.event;

import com.redstonedev.goatexe.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class ForgeEvents {

    private static final Random RNG = new Random();
    private int tickCounter = 0;

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        GoatCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;
        tickCounter++;
        if (tickCounter % 100 != 0) return; // ~5s
        for (ServerLevel level : event.getServer().getAllLevels()) {
            trySpawn(level);
        }
    }

    private boolean hasGoat(ServerLevel level) {
        return !level.getEntities(ModEntities.NIGHTMARE_GOAT.get(), w -> !w.isRemoved()).isEmpty();
    }

    private void trySpawn(ServerLevel level) {
        List<? extends ServerPlayer> players = level.players();
        if (players.isEmpty() || hasGoat(level)) return;
        for (ServerPlayer player : players) {
            boolean night = !level.isDay();
            int chance = night ? 300 : 700;
            if (RNG.nextInt(chance) != 0) continue;
            BlockPos pos = pickSpawnPos(level, player);
            if (pos == null) continue;
            NightmareGoatSpawn(level, pos);
            return;
        }
    }

    private void NightmareGoatSpawn(ServerLevel level, BlockPos pos) {
        com.redstonedev.goatexe.entity.NightmareGoatEntity goat = ModEntities.NIGHTMARE_GOAT.get().create(level);
        if (goat == null) return;
        goat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                level.getRandom().nextFloat() * 360F, 0);
        goat.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(goat);
    }

    private BlockPos pickSpawnPos(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = RNG.nextDouble() * Math.PI * 2.0;
            double dist = 12 + RNG.nextInt(20);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * dist);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * dist);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos c = new BlockPos(x, y, z);
            if (level.getBlockState(c).isAir() && level.getBlockState(c.above()).isAir()
                    && !level.getBlockState(c.below()).isAir()) {
                return c;
            }
        }
        return null;
    }
}
