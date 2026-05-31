package com.redstonedev.goatexe.init;

import com.redstonedev.goatexe.GoatExe;
import com.redstonedev.goatexe.entity.NightmareGoatEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GoatExe.MODID);

    public static final RegistryObject<EntityType<NightmareGoatEntity>> NIGHTMARE_GOAT =
            ENTITIES.register("nightmare_goat", () -> EntityType.Builder
                    .<NightmareGoatEntity>of(NightmareGoatEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.2F)
                    .clientTrackingRange(16)
                    .build(new ResourceLocation(GoatExe.MODID, "nightmare_goat").toString()));
}
