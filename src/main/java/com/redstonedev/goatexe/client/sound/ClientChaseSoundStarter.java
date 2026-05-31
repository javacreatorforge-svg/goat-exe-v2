package com.redstonedev.goatexe.client.sound;

import com.redstonedev.goatexe.entity.NightmareGoatEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientChaseSoundStarter {
    private ClientChaseSoundStarter() {}
    public static void start(NightmareGoatEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new NightmareGoatChaseSoundInstance(entity));
    }
}
