package com.redstonedev.goatexe.client.model;

import com.redstonedev.goatexe.GoatExe;
import com.redstonedev.goatexe.entity.NightmareGoatEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class NightmareGoatModel extends AnimatedGeoModel<NightmareGoatEntity> {
    private static final ResourceLocation MODEL =
            new ResourceLocation(GoatExe.MODID, "geo/nightmare_goat.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(GoatExe.MODID, "textures/entity/nightmare_goat.png");
    private static final ResourceLocation ANIMATIONS =
            new ResourceLocation(GoatExe.MODID, "animations/nightmare_goat.animation.json");

    @Override public ResourceLocation getModelResource(NightmareGoatEntity e)     { return MODEL; }
    @Override public ResourceLocation getTextureResource(NightmareGoatEntity e)   { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(NightmareGoatEntity e) { return ANIMATIONS; }
}
