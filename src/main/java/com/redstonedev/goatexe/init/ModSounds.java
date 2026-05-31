package com.redstonedev.goatexe.init;

import com.redstonedev.goatexe.GoatExe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GoatExe.MODID);

    public static final RegistryObject<SoundEvent> CHASE_THEME = register("chase_theme");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> new SoundEvent(new ResourceLocation(GoatExe.MODID, name)));
    }
}
