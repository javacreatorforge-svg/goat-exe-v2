package com.redstonedev.goatexe.init;

import com.redstonedev.goatexe.GoatExe;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GoatExe.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> NIGHTMARE_GOAT_SPAWN_EGG =
            ITEMS.register("nightmare_goat_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.NIGHTMARE_GOAT,
                            0xCCC1C1,  // pale grey
                            0x000000,  // black
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
