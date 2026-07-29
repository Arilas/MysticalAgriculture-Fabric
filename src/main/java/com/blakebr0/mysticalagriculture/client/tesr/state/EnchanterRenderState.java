package com.blakebr0.mysticalagriculture.client.tesr.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public class EnchanterRenderState extends BlockEntityRenderState {
    public Direction facing;
    public ItemVariant itemResource = ItemVariant.blank();
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
}
