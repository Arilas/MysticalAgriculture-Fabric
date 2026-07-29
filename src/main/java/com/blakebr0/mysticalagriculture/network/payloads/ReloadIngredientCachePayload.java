package com.blakebr0.mysticalagriculture.network.payloads;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ReloadIngredientCachePayload(Map<RecipeType<?>, Map<Item, List<Ingredient>>> caches, Set<Item> validVesselItems) implements CustomPacketPayload {
    public static final Type<ReloadIngredientCachePayload> TYPE = new Type<>(MysticalAgricultureAPI.resource("reload_ingredient_cache"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadIngredientCachePayload> STREAM_CODEC = StreamCodec.of(
            ReloadIngredientCachePayload::toNetwork, ReloadIngredientCachePayload::fromNetwork
    );

    @Override
    public Type<ReloadIngredientCachePayload> type() {
        return TYPE;
    }

    private static ReloadIngredientCachePayload fromNetwork(RegistryFriendlyByteBuf buffer) {
        var caches = new HashMap<RecipeType<?>, Map<Item, List<Ingredient>>>();
        var types = buffer.readVarInt();

        for (var i = 0; i < types; i++) {
            var type = BuiltInRegistries.RECIPE_TYPE.getValue(buffer.readIdentifier());
            var items = buffer.readVarInt();

            caches.put(type, new HashMap<>());

            for (var j = 0; j < items; j++) {
                var item = BuiltInRegistries.ITEM.getValue(buffer.readIdentifier());
                var ingredients = buffer.readVarInt();

                for (var k = 0; k < ingredients; k++) {
                    var cache = caches.get(type).computeIfAbsent(item, _ -> new ArrayList<>());
                    var ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);

                    cache.add(ingredient);
                }
            }
        }

        var validVesselItems = new HashSet<Item>();
        var items = buffer.readVarInt();

        for (var i = 0; i < items; i++) {
            var item = BuiltInRegistries.ITEM.getValue(buffer.readIdentifier());

            validVesselItems.add(item);
        }

        return new ReloadIngredientCachePayload(caches, validVesselItems);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ReloadIngredientCachePayload payload) {
        buffer.writeVarInt(payload.caches.size());

        for (var entry : payload.caches.entrySet()) {
            var type = BuiltInRegistries.RECIPE_TYPE.getKey(entry.getKey());
            var caches = entry.getValue();

            assert type != null;

            buffer.writeIdentifier(type);
            buffer.writeVarInt(caches.size());

            for (var cache : caches.entrySet()) {
                var item = BuiltInRegistries.ITEM.getKey(cache.getKey());
                var ingredients = cache.getValue();

                buffer.writeIdentifier(item);
                buffer.writeVarInt(ingredients.size());

                for (var ingredient : ingredients) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                }
            }
        }

        buffer.writeVarInt(payload.validVesselItems.size());

        for (var item : payload.validVesselItems) {
            var id = BuiltInRegistries.ITEM.getKey(item);

            buffer.writeIdentifier(id);
        }
    }
}
