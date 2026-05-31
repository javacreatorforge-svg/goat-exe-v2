package com.redstonedev.goatexe.client;

import com.redstonedev.goatexe.client.renderer.NightmareGoatRenderer;
import com.redstonedev.goatexe.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.NIGHTMARE_GOAT.get(), NightmareGoatRenderer::new);
        });
    }
}
