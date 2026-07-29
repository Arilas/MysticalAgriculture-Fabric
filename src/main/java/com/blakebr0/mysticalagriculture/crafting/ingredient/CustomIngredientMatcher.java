package com.blakebr0.mysticalagriculture.crafting.ingredient;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public final class CustomIngredientMatcher {
    public static final Identifier CROP_COMPONENT_ID =
            MysticalAgricultureAPI.resource("crop_component");
    public static final Identifier FILLED_SOUL_JAR_ID =
            MysticalAgricultureAPI.resource("filled_soul_jar");

    public static boolean matchesComponents(
            ItemStack input,
            Stream<Holder<Item>> values,
            DataComponentPatch components
    ) {
        if (input == null || values.noneMatch(input::is)) {
            return false;
        }

        for (var entry : components.entrySet()) {
            var type = entry.getKey();
            var value = entry.getValue();
            if ((value.isEmpty() && input.get(type) != null)
                    || (value.isPresent() && !value.get().equals(input.get(type)))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isFilledSoulJar(ItemStack stack, ResourceKey<Item> soulJar) {
        return stack != null && stack.is(soulJar) && MobSoulUtils.getSouls(stack) > 0;
    }

    private CustomIngredientMatcher() {
    }
}
