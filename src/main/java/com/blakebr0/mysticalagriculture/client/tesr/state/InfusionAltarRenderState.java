package com.blakebr0.mysticalagriculture.client.tesr.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import java.util.List;
import java.util.stream.IntStream;

public class InfusionAltarRenderState extends BlockEntityRenderState {
    public ItemVariant itemResource = ItemVariant.blank();
    public List<BlockPos> pedestalPositions;
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public BlockModelRenderState[] blockModelRenderStates = IntStream.range(0, 8).mapToObj(_ -> new BlockModelRenderState()).toArray(BlockModelRenderState[]::new);
    public boolean[] renderGhosts = new boolean[8];
}
