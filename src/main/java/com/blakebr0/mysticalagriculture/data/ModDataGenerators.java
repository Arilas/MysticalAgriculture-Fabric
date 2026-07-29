package com.blakebr0.mysticalagriculture.data;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.data.generator.BlockModelJsonGenerator;
import com.blakebr0.mysticalagriculture.data.generator.BlockTagsJsonGenerator;
import com.blakebr0.mysticalagriculture.data.generator.ItemModelJsonGenerator;
import com.blakebr0.mysticalagriculture.data.generator.ItemTagsJsonGenerator;
import com.blakebr0.mysticalagriculture.data.generator.RecipeJsonGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class ModDataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();

        pack.addProvider((FabricDataGenerator.Pack.Factory<BlockModelJsonGenerator>) output ->
                new BlockModelJsonGenerator(output, MysticalAgricultureAPI.MOD_ID));
        pack.addProvider((FabricDataGenerator.Pack.Factory<ItemModelJsonGenerator>) output ->
                new ItemModelJsonGenerator(output, MysticalAgricultureAPI.MOD_ID));
        pack.addProvider(RecipeJsonGenerator::new);
        pack.addProvider((output, lookup) ->
                new BlockTagsJsonGenerator(output, lookup, MysticalAgricultureAPI.MOD_ID));
        pack.addProvider((output, lookup) ->
                new ItemTagsJsonGenerator(output, lookup, MysticalAgricultureAPI.MOD_ID));
    }
}
