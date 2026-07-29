package com.blakebr0.mysticalagriculture.data.generator;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureTags;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class BlockTagsJsonGenerator extends TagsProvider<Block> {
    public BlockTagsJsonGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, String modId) {
        super(output, Registries.BLOCK, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var crop : CropRegistry.getInstance().getCrops()) {
            this.tag(MysticalAgricultureTags.Blocks.CROPS)
                    .add(crop.getCropBlock().builtInRegistryHolder().key());
        }
    }
}
