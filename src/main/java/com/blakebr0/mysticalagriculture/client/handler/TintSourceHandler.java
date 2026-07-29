package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.cucumber.iface.IColored;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.block.InfusedFarmlandBlock;
import com.blakebr0.mysticalagriculture.client.tints.AugmentTintSource;
import com.blakebr0.mysticalagriculture.client.tints.CropTintSource;
import com.blakebr0.mysticalagriculture.client.tints.InfusionCrystalTintSource;
import com.blakebr0.mysticalagriculture.client.tints.SoulJarTintSource;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.item.ItemTintSources;

import java.util.List;

public final class TintSourceHandler {
    private static boolean registered;

    private TintSourceHandler() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        BlockColorRegistry.register(List.of(new IColored.BlockColors()), InfusedFarmlandBlock.FARMLANDS.toArray(new InfusedFarmlandBlock[0]));

        for (var crop : CropRegistry.getInstance().getCrops()) {
            if (crop.isFlowerColored() && crop.getCropBlock() != null)
                BlockColorRegistry.register(List.of(_ -> crop.getFlowerColor()), crop.getCropBlock());
        }

        ItemTintSources.ID_MAPPER.put(MysticalAgriculture.resource("augment"), AugmentTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(MysticalAgriculture.resource("crop"), CropTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(MysticalAgriculture.resource("infusion_crystal"), InfusionCrystalTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(MysticalAgriculture.resource("soul_jar"), SoulJarTintSource.MAP_CODEC);
        registered = true;
    }
}
