package com.blakebr0.mysticalagriculture.data.generator;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class BlockModelJsonGenerator implements DataProvider {
    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider blockModels;

    public BlockModelJsonGenerator(PackOutput output, String modId) {
        this.blockStates = output.createPathProvider(
                PackOutput.Target.RESOURCE_PACK,
                "blockstates"
        );
        this.blockModels = output.createPathProvider(
                PackOutput.Target.RESOURCE_PACK,
                "models/block"
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        var writes = new ArrayList<CompletableFuture<?>>();

        for (var crop : CropRegistry.getInstance().getCrops()) {
            if (!crop.shouldRegisterCropBlock()) {
                continue;
            }

            var blockId = BuiltInRegistries.BLOCK.getKey(crop.getCropBlock());
            var stemModel = crop.getType().getStemModel();

            var variants = new JsonObject();
            for (var stage = 0; stage <= 7; stage++) {
                var variant = new JsonObject();
                var model = stage == 7
                        ? Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath())
                        : stemModel.withSuffix("_" + stage);
                variant.addProperty("model", model.toString());
                variants.add("age=" + stage, variant);
            }
            var blockState = new JsonObject();
            blockState.add("variants", variants);
            writes.add(DataProvider.saveStable(
                    output,
                    blockState,
                    this.blockStates.json(blockId)
            ));

            var textures = new JsonObject();
            textures.addProperty("flower", crop.getModels().getFlowerModel().toString());
            var model = new JsonObject();
            model.addProperty("parent", stemModel.withSuffix("_7").toString());
            model.add("textures", textures);
            writes.add(DataProvider.saveStable(
                    output,
                    model,
                    this.blockModels.json(blockId)
            ));
        }

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Mystical Agriculture block model generator";
    }
}
