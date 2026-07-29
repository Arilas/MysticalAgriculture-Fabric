package com.blakebr0.mysticalagriculture.task4gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class MachineStorageLookupGameTests {
    @GameTest
    public void lookupPreservesMachinePoliciesCapacityAndTransactions(GameTestHelper helper) {
        var position = helper.absolutePos(BlockPos.ZERO);
        var state = Blocks.BARRIER.defaultBlockState();
        var machine = new TestMachineBlockEntity(position, state);
        machine.setLevel(helper.getLevel());

        require(find(helper, position, state, machine, null) == null,
                "a null side exposed unrestricted storage");

        for (var direction : Direction.Plane.HORIZONTAL) {
            var storage = requireStorage(find(helper, position, state, machine, direction), direction);
            try (var transaction = Transaction.openOuter()) {
                require(storage.insert(ItemVariant.of(Items.COAL), 1, transaction) == 1,
                        direction + " did not use the canonical horizontal fuel policy");
            }
            require(machine.inventory().getAmountAsLong(1) == 0,
                    direction + " retained an aborted fuel insertion");
        }

        var top = requireStorage(find(helper, position, state, machine, Direction.UP), Direction.UP);
        var bottom = requireStorage(find(helper, position, state, machine, Direction.DOWN), Direction.DOWN);
        var diamond = ItemVariant.of(Items.DIAMOND);

        try (var transaction = Transaction.openOuter()) {
            require(top.insert(diamond, 600, transaction) == 512,
                    "the provider lost the extended 512-item slot capacity");
            require(machine.notifications() == 0,
                    "the provider notified before the outer transaction committed");
            transaction.commit();
        }
        require(machine.notifications() == 1,
                "the committed provider mutation did not notify exactly once");
        require(machine.inventory().getAmountAsLong(0) == 512,
                "the provider did not retain the committed extended stack");

        try (var transaction = Transaction.openOuter()) {
            require(top.extract(diamond, 1, transaction) == 0,
                    "the top face extracted a protected input");
            require(bottom.insert(diamond, 1, transaction) == 0,
                    "the bottom face accepted input");
        }

        var gold = ItemVariant.of(Items.GOLD_INGOT);
        machine.inventory().set(2, gold, 1);
        try (var transaction = Transaction.openOuter()) {
            require(bottom.extract(gold, 1, transaction) == 1,
                    "the bottom face rejected output extraction");
            transaction.commit();
        }

        helper.succeed();
    }

    private static SlottedStorage<ItemVariant> find(
            GameTestHelper helper,
            BlockPos position,
            net.minecraft.world.level.block.state.BlockState state,
            TestMachineBlockEntity machine,
            Direction side
    ) {
        var storage = ItemStorage.SIDED.find(helper.getLevel(), position, state, machine, side);
        return storage instanceof SlottedStorage<ItemVariant> slotted ? slotted : null;
    }

    private static SlottedStorage<ItemVariant> requireStorage(
            SlottedStorage<ItemVariant> storage,
            Direction side
    ) {
        require(storage != null, side + " did not expose machine storage through ItemStorage.SIDED");
        return storage;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
