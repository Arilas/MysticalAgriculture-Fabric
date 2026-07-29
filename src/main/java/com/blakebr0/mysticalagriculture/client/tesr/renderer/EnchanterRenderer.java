package com.blakebr0.mysticalagriculture.client.tesr.renderer;

import com.blakebr0.mysticalagriculture.block.EnchanterBlock;
import com.blakebr0.mysticalagriculture.client.tesr.state.EnchanterRenderState;
import com.blakebr0.mysticalagriculture.tileentity.EnchanterTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnchanterRenderer implements BlockEntityRenderer<EnchanterTileEntity, EnchanterRenderState> {
    private final ItemModelResolver itemModelResolver;

    public EnchanterRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EnchanterRenderState createRenderState() {
        return new EnchanterRenderState();
    }

    @Override
    public void extractRenderState(EnchanterTileEntity tile, EnchanterRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);

        state.facing = tile.getBlockState().getValue(EnchanterBlock.FACING);
        state.itemResource = tile.getInventory().getResource(2);

        this.itemModelResolver.updateForTopItem(state.itemRenderState, state.itemResource.toStack(), ItemDisplayContext.FIXED, tile.getLevel(), null, 0);
    }

    @Override
    public void submit(EnchanterRenderState state, PoseStack matrix, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.itemResource.isBlank() && state.facing != null) {
            matrix.pushPose();
            matrix.translate(0.5D, 0.89D, 0.5D);

            float scale = 0.7F;

            matrix.scale(scale, scale, scale);

            var axis = state.facing.getAxis();
            var axisDirection = state.facing.getAxisDirection().getStep();
            int index = state.facing.get2DDataValue();

            if (axis == Direction.Axis.X) {
                matrix.mulPose(Axis.ZP.rotationDegrees(158 * axisDirection));
                matrix.mulPose(Axis.YP.rotationDegrees(180 + (-90 * index)));
            } else if (axis == Direction.Axis.Z) {
                matrix.mulPose(Axis.XN.rotationDegrees(158 * axisDirection));
                matrix.mulPose(Axis.YP.rotationDegrees(180 - (-90 * index)));
            }

            matrix.mulPose(Axis.XP.rotationDegrees(-90));

            matrix.translate(0.0D, -0.08D, 0.0D);

            state.itemRenderState.submit(matrix, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            matrix.popPose();
        }
    }
}
