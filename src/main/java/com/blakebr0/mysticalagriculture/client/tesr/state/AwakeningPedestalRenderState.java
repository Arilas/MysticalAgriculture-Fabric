package com.blakebr0.mysticalagriculture.client.tesr.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public class AwakeningPedestalRenderState extends BlockEntityRenderState {
    public ItemVariant itemResource = ItemVariant.blank();
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
}
