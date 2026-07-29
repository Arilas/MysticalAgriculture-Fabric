package com.blakebr0.mysticalagriculture.client.tesr.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public class EssenceVesselRenderState extends BlockEntityRenderState {
    public ItemVariant itemResource = ItemVariant.blank();
    public float fillPercentage;
}
