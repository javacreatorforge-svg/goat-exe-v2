package com.redstonedev.goatexe.client.renderer;

import com.redstonedev.goatexe.client.model.NightmareGoatModel;
import com.redstonedev.goatexe.entity.NightmareGoatEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class NightmareGoatRenderer extends GeoEntityRenderer<NightmareGoatEntity> {
    public NightmareGoatRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NightmareGoatModel());
        this.shadowRadius = 0.5F;
    }
}
