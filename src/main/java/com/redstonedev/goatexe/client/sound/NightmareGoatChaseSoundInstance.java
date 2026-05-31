package com.redstonedev.goatexe.client.sound;

import com.redstonedev.goatexe.entity.NightmareGoatEntity;
import com.redstonedev.goatexe.init.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NightmareGoatChaseSoundInstance extends AbstractTickableSoundInstance {
    private final NightmareGoatEntity goat;

    public NightmareGoatChaseSoundInstance(NightmareGoatEntity entity) {
        super(ModSounds.CHASE_THEME.get(), SoundSource.HOSTILE, RandomSource.create());
        this.goat = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.attenuation = Attenuation.LINEAR;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        if (goat.isRemoved() || !goat.isAlive() || !goat.isAggressiveMode()) {
            this.stop();
            return;
        }
        this.x = goat.getX();
        this.y = goat.getY();
        this.z = goat.getZ();
    }
}
