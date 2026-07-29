package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.crafting.ingredient.CropComponentIngredient;
import com.blakebr0.mysticalagriculture.crafting.ingredient.FilledSoulJarIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public final class ModIngredientTypes {
    public static void register() {
        CustomIngredientSerializer.register(FilledSoulJarIngredient.SERIALIZER);
        CustomIngredientSerializer.register(CropComponentIngredient.SERIALIZER);
    }

    private ModIngredientTypes() {
    }
}
