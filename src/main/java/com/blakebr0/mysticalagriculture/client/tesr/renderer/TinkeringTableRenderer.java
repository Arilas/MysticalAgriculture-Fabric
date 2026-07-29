package com.blakebr0.mysticalagriculture.client.tesr.renderer;

import com.blakebr0.mysticalagriculture.block.TinkeringTableBlock;
import com.blakebr0.mysticalagriculture.client.tesr.state.TinkeringTableRenderState;
import com.blakebr0.mysticalagriculture.tileentity.TinkeringTableTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TinkeringTableRenderer implements BlockEntityRenderer<TinkeringTableTileEntity, TinkeringTableRenderState> {
    private final ItemModelResolver itemModelResolver;

    public TinkeringTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public TinkeringTableRenderState createRenderState() {
        return new TinkeringTableRenderState();
    }

    @Override
    public void extractRenderState(TinkeringTableTileEntity tile, TinkeringTableRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);

        state.facing = tile.getBlockState().getValue(TinkeringTableBlock.FACING);
        state.itemResource = tile.getInventory().getResource(0);

        int seed = HashCommon.long2int(state.blockPos.asLong());

        this.itemModelResolver.updateForTopItem(state.itemRenderState,  state.itemResource.toStack(), ItemDisplayContext.FIXED, tile.getLevel(), null, seed);
    }

    @Override
    public void submit(TinkeringTableRenderState state, PoseStack matrix, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.itemResource.isBlank() && state.facing != null) {
            matrix.pushPose();
            matrix.translate(0.5D, 0.9D, 0.5D);
            float scale = 0.7F;
            matrix.scale(scale, scale, scale);
            int index = state.facing.get2DDataValue();
            matrix.mulPose(Axis.YP.rotationDegrees(-90 * index));
            matrix.mulPose(Axis.XP.rotationDegrees(90));
            state.itemRenderState.submit(matrix, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrix.popPose();
        }
    }
}
