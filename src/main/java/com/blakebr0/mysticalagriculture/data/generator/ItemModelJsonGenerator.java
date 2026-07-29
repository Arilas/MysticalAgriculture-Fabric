package com.blakebr0.mysticalagriculture.data.generator;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.registry.AugmentRegistry;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class ItemModelJsonGenerator implements DataProvider {
    private final PackOutput.PathProvider itemDefinitions;
    private final PackOutput.PathProvider itemModels;

    public ItemModelJsonGenerator(PackOutput output, String modId) {
        this.itemDefinitions = output.createPathProvider(
                PackOutput.Target.RESOURCE_PACK,
                "items"
        );
        this.itemModels = output.createPathProvider(
                PackOutput.Target.RESOURCE_PACK,
                "models/item"
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        var writes = new ArrayList<CompletableFuture<?>>();

        for (var crop : CropRegistry.getInstance().getCrops()) {
            if (crop.shouldRegisterEssenceItem()) {
                addFlatItem(
                        output,
                        writes,
                        BuiltInRegistries.ITEM.getKey(crop.getEssenceItem()),
                        crop.getModels().getEssenceModel()
                );
            }
            if (crop.shouldRegisterSeedsItem()) {
                addFlatItem(
                        output,
                        writes,
                        BuiltInRegistries.ITEM.getKey(crop.getSeedsItem()),
                        crop.getModels().getSeedModel()
                );
            }
        }

        for (var augment : AugmentRegistry.getInstance().getAugments()) {
            var itemId = BuiltInRegistries.ITEM.getKey(augment.getItem());

            var textures = new JsonObject();
            textures.addProperty(
                    "layer1",
                    MysticalAgricultureAPI.resource("item/augment_" + augment.getTier()).toString()
            );
            var model = new JsonObject();
            model.addProperty("parent", MysticalAgricultureAPI.resource("item/augment").toString());
            model.add("textures", textures);
            writes.add(DataProvider.saveStable(
                    output,
                    model,
                    this.itemModels.json(itemId)
            ));

            var tints = new JsonArray();
            tints.add(augmentTint(augment.getId(), 0));
            tints.add(augmentTint(augment.getId(), 1));
            writes.add(DataProvider.saveStable(
                    output,
                    itemDefinition(itemId, tints),
                    this.itemDefinitions.json(itemId)
            ));
        }

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private void addFlatItem(
            CachedOutput output,
            ArrayList<CompletableFuture<?>> writes,
            Identifier itemId,
            Identifier texture
    ) {
        var textures = new JsonObject();
        textures.addProperty("layer0", texture.toString());
        var model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");
        model.add("textures", textures);
        writes.add(DataProvider.saveStable(
                output,
                model,
                this.itemModels.json(itemId)
        ));
        writes.add(DataProvider.saveStable(
                output,
                itemDefinition(itemId, null),
                this.itemDefinitions.json(itemId)
        ));
    }

    private static JsonObject itemDefinition(Identifier itemId, JsonArray tints) {
        var model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty(
                "model",
                Identifier.fromNamespaceAndPath(
                        itemId.getNamespace(),
                        "item/" + itemId.getPath()
                ).toString()
        );
        if (tints != null) {
            model.add("tints", tints);
        }

        var definition = new JsonObject();
        definition.add("model", model);
        return definition;
    }

    private static JsonObject augmentTint(Identifier augment, int index) {
        var tint = new JsonObject();
        tint.addProperty("type", MysticalAgricultureAPI.resource("augment").toString());
        tint.addProperty("id", augment.toString());
        tint.addProperty("index", index);
        return tint;
    }

    @Override
    public String getName() {
        return "Mystical Agriculture item model generator";
    }
}
