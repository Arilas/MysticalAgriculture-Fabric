package com.blakebr0.mysticalagriculture.network;

import com.blakebr0.mysticalagriculture.network.payloads.ExperienceCapsulePickupPayload;
import com.blakebr0.mysticalagriculture.network.payloads.ReloadIngredientCachePayload;
import com.blakebr0.mysticalagriculture.network.payloads.SyncEssenceVesselColorsPayload;
import com.blakebr0.mysticalagriculture.network.payloads.UpdateAOEAugmentOffsetPayload;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadCodecTest {
    @BeforeAll
    static void bootstrapRegistries() {
        Task3TestRegistries.ensureInitialized();
    }

    @Test
    void experiencePickupCodecConsumesTheWholePayload() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());

        ExperienceCapsulePickupPayload.STREAM_CODEC.encode(buffer, new ExperienceCapsulePickupPayload());
        assertEquals(new ExperienceCapsulePickupPayload(),
                ExperienceCapsulePickupPayload.STREAM_CODEC.decode(buffer));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void ingredientCacheCodecRoundTripsRecipesAndVesselItems() {
        var original = new ReloadIngredientCachePayload(
                Map.of(RecipeType.SMELTING, Map.of(Items.DIAMOND, List.of(Ingredient.of(Items.STONE)))),
                Set.of(Items.GOLD_INGOT)
        );
        var buffer = registryBuffer();

        ReloadIngredientCachePayload.STREAM_CODEC.encode(buffer, original);
        var decoded = ReloadIngredientCachePayload.STREAM_CODEC.decode(buffer);

        assertEquals(Set.of(Items.GOLD_INGOT), decoded.validVesselItems());
        var ingredients = decoded.caches().get(RecipeType.SMELTING).get(Items.DIAMOND);
        assertEquals(1, ingredients.size());
        assertTrue(ingredients.getFirst().test(Items.STONE.getDefaultInstance()));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void vesselColorCodecRoundTripsLiteralColors() {
        var original = new SyncEssenceVesselColorsPayload(Map.of(
                "minecraft:diamond", 0x4AEDD9,
                "minecraft:gold_ingot", 0xFDF55F
        ));
        var buffer = registryBuffer();

        SyncEssenceVesselColorsPayload.STREAM_CODEC.encode(buffer, original);

        assertEquals(original, SyncEssenceVesselColorsPayload.STREAM_CODEC.decode(buffer));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void aoeOffsetCodecRoundTripsSingleStepChanges() {
        var original = new UpdateAOEAugmentOffsetPayload(-1, 1);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());

        UpdateAOEAugmentOffsetPayload.STREAM_CODEC.encode(buffer, original);

        assertEquals(original, UpdateAOEAugmentOffsetPayload.STREAM_CODEC.decode(buffer));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void aoeOffsetCodecRejectsOversizedClientDelta() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeInt(2);
        buffer.writeInt(0);

        assertThrows(IllegalArgumentException.class,
                () -> UpdateAOEAugmentOffsetPayload.STREAM_CODEC.decode(buffer));
    }

    @Test
    void aoeOffsetCodecRejectsMinimumIntegerDelta() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeInt(Integer.MIN_VALUE);
        buffer.writeInt(0);

        assertThrows(IllegalArgumentException.class,
                () -> UpdateAOEAugmentOffsetPayload.STREAM_CODEC.decode(buffer));
    }

    private static RegistryFriendlyByteBuf registryBuffer() {
        return new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                Task3TestRegistries.registryAccess()
        );
    }
}
