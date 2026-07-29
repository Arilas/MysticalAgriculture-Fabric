package com.blakebr0.mysticalagriculture.api.lib;

import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

public class LazyIngredient {
    public static final LazyIngredient EMPTY = new LazyIngredient(null, null, null) {
        @Override
        public @Nullable Ingredient getIngredient(HolderLookup.Provider registries) {
            return null;
        }
    };

    private final String id;
    private final DataComponentMap components;
    private final Type type;
    private @Nullable Ingredient ingredient;

    private boolean loadedIngredient = false;

    private LazyIngredient(String id, Type type, DataComponentMap components) {
        this.id = id;
        this.type = type;
        this.components = components;
    }

    public static LazyIngredient item(String name) {
        return item(name, DataComponentMap.EMPTY);
    }

    public static LazyIngredient item(String name, DataComponentMap components) {
        return new LazyIngredient(name, Type.ITEM, components);
    }

    public static LazyIngredient tag(String name) {
        return new LazyIngredient(name, Type.TAG, DataComponentMap.EMPTY);
    }
    
    public String getId() {
        return this.id;
    }

    public boolean isItem() {
        return this.type == Type.ITEM;
    }

    public boolean isTag() {
        return this.type == Type.TAG;
    }

    public @Nullable Ingredient getIngredient(HolderLookup.Provider registries) {
        if (!this.loadedIngredient) {
            if (this.isTag()) {
                registries.get(TagKey.create(Registries.ITEM, Identifier.parse(this.id))).ifPresent(item -> {
                    this.ingredient = Ingredient.of(item);
                });
            } else if (this.isItem()) {
                BuiltInRegistries.ITEM.get(Identifier.parse(this.id)).ifPresent(item -> {
                    if (this.components == null || this.components.isEmpty()) {
                        this.ingredient = Ingredient.of(item.value());
                    } else {
                        this.ingredient = DefaultCustomIngredients.components(
                                Ingredient.of(item.value()),
                                builder -> builder.set(this.components)
                        );
                    }
                });
            }

            this.loadedIngredient = true;
        }

        return this.ingredient;
    }

    public DataComponentMap getComponents() {
        return this.components;
    }

    private enum Type {
        ITEM, TAG
    }
}
