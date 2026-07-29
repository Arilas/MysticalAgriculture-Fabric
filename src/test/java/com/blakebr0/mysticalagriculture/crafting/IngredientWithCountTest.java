package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientWithCountTest {
    private static RegistryAccess registryAccess;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
                .forEach(pending -> pending.apply());
    }

    @Test
    void jsonRoundTripPreservesLiteralIngredientAndCountFields() {
        var json = JsonParser.parseString("""
                {
                  "ingredient": "minecraft:diamond",
                  "count": 3
                }
                """);
        var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

        var decoded = IngredientWithCount.CODEC.parse(ops, json).getOrThrow();
        var encoded = IngredientWithCount.CODEC.encodeStart(ops, decoded).getOrThrow().getAsJsonObject();

        assertEquals(3, decoded.count());
        assertTrue(decoded.test(new ItemStack(Items.DIAMOND, 3)));
        assertFalse(decoded.test(new ItemStack(Items.DIAMOND, 2)));
        assertEquals("minecraft:diamond", encoded.get("ingredient").getAsString());
        assertEquals(3, encoded.get("count").getAsInt());
    }

    @Test
    void countsBelowOneAreRejectedByConstructionAndJsonCodec() {
        assertThrows(IllegalArgumentException.class,
                () -> new IngredientWithCount(Ingredient.of(Items.DIAMOND), 0));

        var result = IngredientWithCount.CODEC.parse(
                RegistryOps.create(JsonOps.INSTANCE, registryAccess),
                JsonParser.parseString("""
                        {
                          "ingredient": "minecraft:diamond",
                          "count": 0
                        }
                        """)
        );

        assertTrue(result.isError(), "count zero must be rejected during JSON decoding");
    }

    @Test
    void networkRoundTripPreservesIngredientAndCount() {
        var expected = new IngredientWithCount(Ingredient.of(Items.EMERALD), 7);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);

        IngredientWithCount.STREAM_CODEC.encode(buffer, expected);
        var decoded = IngredientWithCount.STREAM_CODEC.decode(buffer);

        assertEquals(7, decoded.count());
        assertTrue(decoded.ingredient().test(new ItemStack(Items.EMERALD)));
        assertEquals(0, buffer.readableBytes());
    }
}
