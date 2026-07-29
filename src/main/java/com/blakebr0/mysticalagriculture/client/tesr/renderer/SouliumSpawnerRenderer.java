package com.blakebr0.mysticalagriculture.client.tesr.renderer;

import com.blakebr0.mysticalagriculture.client.tesr.state.SouliumSpawnerRenderState;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SouliumSpawnerRenderer implements BlockEntityRenderer<SouliumSpawnerTileEntity, SouliumSpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public SouliumSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public SouliumSpawnerRenderState createRenderState() {
        return new SouliumSpawnerRenderState();
    }

    @Override
    public void extractRenderState(SouliumSpawnerTileEntity tile, SouliumSpawnerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.partialTicks = partialTicks;

        var displayEntity = tile.getDisplayEntity();
        if (displayEntity != null) {
            var entity = displayEntity.entity();

            state.displayEntity = this.entityRenderer.extractEntity(entity, state.partialTicks);
            state.displayEntity.lightCoords = state.lightCoords;
            state.bbWidth = entity.getBbWidth();
            state.bbHeight = entity.getBbHeight();
        } else {
            state.displayEntity = null;
        }

        state.spin = tile.getSpin();
        state.oSpin = tile.getoSpin();
    }

    @Override
    public void submit(SouliumSpawnerRenderState state, PoseStack matrix, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrix.pushPose();
        matrix.translate(0.5F, 0.0F, 0.5F);

        if (state.displayEntity != null) {
            float scale = 0.53125F;
            float bbMax = Math.max(state.bbWidth, state.bbHeight);

            if ((double) bbMax > 1.0D) {
                scale /= bbMax;
            }

            matrix.translate(0.0F, 0.4F, 0.0F);
            matrix.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp(state.partialTicks, state.oSpin, state.spin) * 10.0F));
            matrix.translate(0.0F, -0.2F, 0.0F);
            matrix.mulPose(Axis.XP.rotationDegrees(-30.0F));
            matrix.scale(scale, scale, scale);

            this.entityRenderer.submit(state.displayEntity, camera, 0.0, 0.0, 0.0, matrix, submitNodeCollector);
        }

        matrix.popPose();
    }
}
