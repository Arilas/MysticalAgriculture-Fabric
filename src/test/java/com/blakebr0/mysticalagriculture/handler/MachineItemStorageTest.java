package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.SidedInventoryWrapper;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MachineItemStorageTest {
    @BeforeAll
    static void bootstrapRegistries() {
        Task3TestRegistries.ensureInitialized();
    }

    @Test
    void everyHorizontalFaceUsesTheCanonicalFuelWrapper() {
        var inventory = CItemStacksHandler.create(3);
        var wrappers = SidedInventoryWrapper.create(
                inventory,
                List.of(Direction.UP, Direction.DOWN, Direction.NORTH),
                (slot, resource, side) -> slot == 1 && side == Direction.NORTH && resource.isOf(Items.COAL),
                (_, _) -> false
        );

        for (var face : Direction.Plane.HORIZONTAL) {
            SlottedStorage<ItemVariant> storage = MachineItemStorage.sided(wrappers, face);
            assertNotNull(storage, () -> face + " did not expose machine storage");

            try (var transaction = Transaction.openOuter()) {
                assertEquals(1, storage.insert(ItemVariant.of(Items.COAL), 1, transaction),
                        () -> face + " did not use the canonical horizontal fuel policy");
            }

            assertEquals(0, inventory.getAmountAsLong(1),
                    () -> face + " retained an aborted test insertion");
        }
    }

    @Test
    void adapterPreservesOutputInsertionAndInputExtractionRestrictions() {
        var inventory = CItemStacksHandler.create(3, builder -> builder.setOutputSlots(2));
        var storage = MachineItemStorage.of(inventory);
        var input = ItemVariant.of(Items.DIAMOND);
        var output = ItemVariant.of(Items.GOLD_INGOT);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(1, inventory.insert(0, input, 1, transaction, true));
            assertEquals(1, inventory.insert(2, output, 1, transaction, true));
            transaction.commit();
        }

        try (var transaction = Transaction.openOuter()) {
            assertEquals(0, storage.getSlot(2).insert(output, 1, transaction),
                    "an output-only slot accepted external insertion");
            assertEquals(0, storage.getSlot(0).extract(input, 1, transaction),
                    "a protected input slot allowed extraction");
            assertEquals(1, storage.getSlot(2).extract(output, 1, transaction),
                    "an output slot rejected extraction");
            transaction.commit();
        }

        assertEquals(1, inventory.getAmountAsLong(0));
        assertEquals(0, inventory.getAmountAsLong(2));
    }

    @Test
    void adapterPreservesLongPerSlotCapacityAboveTheItemMaximum() {
        var inventory = CItemStacksHandler.create(1, builder -> builder.addSlotLimit(0, 512));
        var storage = MachineItemStorage.of(inventory);

        try (var transaction = Transaction.openOuter()) {
            assertEquals(512, storage.insert(ItemVariant.of(Items.ROTTEN_FLESH), 600, transaction));
            transaction.commit();
        }

        assertEquals(512, storage.getSlot(0).getCapacity());
        assertEquals(512, storage.getSlot(0).getAmount());
    }

    @Test
    void adapterDefersOneNotificationUntilFinalCommitAndRestoresNestedAborts() {
        var notifications = new AtomicInteger();
        var inventory = CItemStacksHandler.create(
                1,
                (_, _) -> notifications.incrementAndGet(),
                _ -> {}
        );
        var storage = MachineItemStorage.of(inventory);
        var stone = ItemVariant.of(Items.STONE);

        try (var outer = Transaction.openOuter()) {
            assertEquals(2, storage.insert(stone, 2, outer));
            try (var nested = outer.openNested()) {
                assertEquals(3, storage.insert(stone, 3, nested));
            }

            assertEquals(2, inventory.getAmountAsLong(0));
            assertEquals(0, notifications.get(), "a callback escaped before final commit");
            outer.commit();
        }

        assertEquals(2, inventory.getAmountAsLong(0));
        assertEquals(1, notifications.get(), "the final commit did not notify exactly once");

        try (var outer = Transaction.openOuter()) {
            assertEquals(2, storage.extract(stone, 2, outer));
        }

        assertEquals(2, inventory.getAmountAsLong(0));
        assertEquals(1, notifications.get(), "an aborted outer transaction emitted a callback");
    }
}
