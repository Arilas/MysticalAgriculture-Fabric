package com.blakebr0.mysticalagriculture.network;

import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.network.payloads.UpdateAOEAugmentOffsetPayload;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AOEUpdateValidatorTest {
    private static final ResourceKey<Item> TINKERABLE_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("mysticalagriculture-test", "tinkerable")
    );
    private static final ResourceKey<Item> WRONG_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("mysticalagriculture-test", "wrong_item")
    );
    private static ItemStack tinkerable;
    private static ItemStack wrongItem;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var tinkerableItem = Registry.register(
                BuiltInRegistries.ITEM,
                TINKERABLE_KEY,
                new TestTinkerableItem()
        );
        var wrong = Registry.register(
                BuiltInRegistries.ITEM,
                WRONG_ITEM_KEY,
                new Item(new Item.Properties().setId(WRONG_ITEM_KEY))
        );
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
                .forEach(pending -> pending.apply());
        tinkerable = new ItemStack(tinkerableItem);
        wrongItem = new ItemStack(wrong);
    }

    @Test
    void rejectsNegativeDeltaLargerThanOneStep() {
        assertFalse(AOEUpdateValidator.isValid(
                tinkerable,
                new UpdateAOEAugmentOffsetPayload(-2, 0),
                _ -> 3
        ));
    }

    @Test
    void rejectsPositiveDeltaLargerThanOneStep() {
        assertFalse(AOEUpdateValidator.isValid(
                tinkerable,
                new UpdateAOEAugmentOffsetPayload(0, 2),
                _ -> 3
        ));
    }

    @Test
    void rejectsTinkerableWithoutInstalledAoeAugment() {
        assertFalse(AOEUpdateValidator.isValid(
                tinkerable,
                new UpdateAOEAugmentOffsetPayload(1, 0),
                _ -> 0
        ));
    }

    @Test
    void rejectsWrongHeldItem() {
        assertFalse(AOEUpdateValidator.isValid(
                wrongItem,
                new UpdateAOEAugmentOffsetPayload(1, 0),
                _ -> 3
        ));
    }

    @Test
    void acceptsSingleStepUpdateForInstalledAoeAugment() {
        assertTrue(AOEUpdateValidator.isValid(
                tinkerable,
                new UpdateAOEAugmentOffsetPayload(-1, 1),
                _ -> 3
        ));
    }

    private static final class TestTinkerableItem extends Item implements ITinkerable {
        private TestTinkerableItem() {
            super(new Item.Properties().setId(TINKERABLE_KEY));
        }

        @Override
        public int getAugmentSlots() {
            return 1;
        }

        @Override
        public EnumSet<AugmentType> getAugmentTypes() {
            return EnumSet.of(AugmentType.PICKAXE);
        }

        @Override
        public int getTinkerableTier() {
            return 1;
        }
    }
}
