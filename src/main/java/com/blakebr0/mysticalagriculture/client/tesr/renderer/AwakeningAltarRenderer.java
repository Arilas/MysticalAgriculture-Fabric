package com.blakebr0.mysticalagriculture.client.tesr.renderer;

import com.blakebr0.cucumber.client.ModRenderTypes;
import com.blakebr0.mysticalagriculture.client.tesr.state.AwakeningAltarRenderState;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningAltarTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AwakeningAltarRenderer implements BlockEntityRenderer<AwakeningAltarTileEntity, AwakeningAltarRenderState> {
    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;

    public AwakeningAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public AwakeningAltarRenderState createRenderState() {
        return new AwakeningAltarRenderState();
    }

    @Override
    public void extractRenderState(AwakeningAltarTileEntity tile, AwakeningAltarRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);

        var inventory = tile.getInventory();

        state.itemResource = inventory.getResource(1).isBlank() ? inventory.getResource(0) : inventory.getResource(1);
        state.pedestalPositions = tile.getPedestalPositions();

        int seed = HashCommon.long2int(state.blockPos.asLong());

        this.itemModelResolver.updateForTopItem(state.itemRenderState,  state.itemResource.toStack(), ItemDisplayContext.FIXED, tile.getLevel(), null, seed);

        var level = tile.getLevel();

        for (int i = 0; i < state.blockModelRenderStates.length; i++) {
            state.renderGhosts[i] = false;
        }

        for (int i = 0; i < Math.min(state.pedestalPositions.size(), state.blockModelRenderStates.length); i++) {
            var aoePos = state.pedestalPositions.get(i);
            if (level != null && level.isEmptyBlock(aoePos)) {
                var block = i % 2 == 0
                        ? ModBlocks.AWAKENING_PEDESTAL.defaultBlockState()
                        : ModBlocks.ESSENCE_VESSEL.defaultBlockState();

                this.blockModelResolver.update(state.blockModelRenderStates[i], block, BlockDisplayContext.create());
                state.renderGhosts[i] = true;
            }
        }
    }

    @Override
    public void submit(AwakeningAltarRenderState state, PoseStack matrix, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.itemResource.isBlank()) {
            matrix.pushPose();
            matrix.translate(0.5D, 1.1D, 0.5D);
            float scale = state.itemResource.getItem() instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof CropBlock) ? 0.55F : 0.35F;
            matrix.scale(scale, scale, scale);
            double tick = System.currentTimeMillis() / 800.0D;
            matrix.translate(0.0D, Math.sin(tick % (2 * Math.PI)) * 0.065D, 0.0D);
            matrix.mulPose(Axis.YP.rotationDegrees((float) ((tick * 40.0D) % 360)));
            state.itemRenderState.submit(matrix, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrix.popPose();
        }

        matrix.pushPose();
        matrix.translate(-state.blockPos.getX(), -state.blockPos.getY(), -state.blockPos.getZ());

        for (int i = 0; i < Math.min(state.blockModelRenderStates.length, state.pedestalPositions.size()); i++) {
            if (!state.renderGhosts[i])
                continue;

            var blockModelRenderState = state.blockModelRenderStates[i];
            var aoePos = state.pedestalPositions.get(i);

            matrix.pushPose();
            matrix.translate(aoePos.getX(), aoePos.getY(), aoePos.getZ());

            blockModelRenderState.renderType = ModRenderTypes.ghostBlock();
            blockModelRenderState.submit(matrix, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            matrix.popPose();
        }

        matrix.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
