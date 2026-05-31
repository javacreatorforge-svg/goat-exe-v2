package com.redstonedev.goatexe;

import com.mojang.logging.LogUtils;
import com.redstonedev.goatexe.client.ClientSetup;
import com.redstonedev.goatexe.entity.NightmareGoatEntity;
import com.redstonedev.goatexe.event.ForgeEvents;
import com.redstonedev.goatexe.init.ModEntities;
import com.redstonedev.goatexe.init.ModItems;
import com.redstonedev.goatexe.init.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(GoatExe.MODID)
public class GoatExe {
    public static final String MODID = "goat_exe";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GoatExe() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::entityAttributes);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Goat.exe loaded - the goat is here");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientSetup.onClientSetup(event);
    }

    private void entityAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntities.NIGHTMARE_GOAT.get(), NightmareGoatEntity.createAttributes().build());
    }
}
