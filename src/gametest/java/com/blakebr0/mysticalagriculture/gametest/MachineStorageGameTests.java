package com.blakebr0.mysticalagriculture.gametest;

import com.blakebr0.cucumber.energy.CEnergyStorage;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeTier;
import com.blakebr0.mysticalagriculture.container.SouliumSpawnerContainer;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModMenuTypes;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class MachineStorageGameTests {
    @GameTest
    public void machineUpgradeInventoryEnforcesItemTypeAndSingleItemLimit(GameTestHelper helper) {
        var inventory = new MachineUpgradeItemStackHandler();

        try (var transaction = Transaction.openOuter()) {
            require(inventory.insert(0, ItemVariant.of(Items.STONE), 1, transaction) == 0,
                    "the upgrade slot accepted a non-upgrade item");
            require(inventory.insert(0, ItemVariant.of(ModItems.INFERIUM_UPGRADE), 2, transaction) == 1,
                    "the upgrade slot did not enforce its single-item limit");
            transaction.commit();
        }

        require(inventory.getUpgradeTier() == MachineUpgradeTier.INFERIUM,
                "the accepted upgrade did not expose its effective tier");
        helper.succeed();
    }

    @GameTest
    public void sidedMachineStorageEnforcesInsertionAndExtractionFaces(GameTestHelper helper) {
        var position = helper.absolutePos(BlockPos.ZERO);
        var machine = new EssenceFurnaceTileEntity(position, ModBlocks.FURNACE.defaultBlockState());
        machine.setLevel(helper.getLevel());
        var input = ItemVariant.of(Items.DIAMOND);
        var output = ItemVariant.of(Items.GOLD_INGOT);
        var top = findStorage(helper, position, machine.getBlockState(), machine, Direction.UP);
        var bottom = findStorage(helper, position, machine.getBlockState(), machine, Direction.DOWN);

        require(findStorage(helper, position, machine.getBlockState(), machine, null) == null,
                "a null side exposed unrestricted machine storage");
        require(top != null && bottom != null, "a supported machine side did not expose storage");

        for (var direction : Direction.Plane.HORIZONTAL) {
            var horizontal = findStorage(helper, position, machine.getBlockState(), machine, direction);
            require(horizontal != null, direction + " did not expose storage");
            try (var transaction = Transaction.openOuter()) {
                require(horizontal.insert(ItemVariant.of(Items.COAL), 1, transaction) == 1,
                        direction + " did not use the canonical horizontal fuel policy");
            }
            require(machine.getInventory().getAmountAsLong(1) == 0,
                    direction + " retained an aborted fuel insertion");
        }

        try (var transaction = Transaction.openOuter()) {
            require(top.insert(input, 1, transaction) == 1, "the top face rejected a valid input");
            transaction.commit();
        }
        try (var transaction = Transaction.openOuter()) {
            require(bottom.insert(input, 1, transaction) == 0, "the bottom face accepted input");
            require(top.extract(input, 1, transaction) == 0, "the top face extracted a protected input");
        }

        machine.getInventory().set(2, output, 1);
        try (var transaction = Transaction.openOuter()) {
            require(bottom.extract(output, 1, transaction) == 1, "the bottom face rejected output extraction");
            transaction.commit();
        }

        helper.succeed();
    }

    @GameTest
    public void outputOnlySlotRejectsExternalInsertion(GameTestHelper helper) {
        var position = helper.absolutePos(BlockPos.ZERO);
        var machine = new ReprocessorTileEntity(position, ModBlocks.REPROCESSOR.defaultBlockState());
        machine.setLevel(helper.getLevel());
        var storage = findStorage(helper, position, machine.getBlockState(), machine, Direction.DOWN);
        require(storage != null, "the reprocessor did not expose bottom storage");

        try (var transaction = Transaction.openOuter()) {
            long inserted = storage.getSlot(2).insert(ItemVariant.of(Items.DIAMOND), 1, transaction);
            require(inserted == 0, "the reprocessor output slot accepted insertion");
            transaction.commit();
        }

        require(machine.getInventory().getAmountAsLong(2) == 0,
                "the rejected insertion changed the output slot");
        helper.succeed();
    }

    @GameTest
    public void oversizedMachineInputStopsAtItsSlotLimit(GameTestHelper helper) {
        var previousCaches = RecipeIngredientCache.INSTANCE.copyCaches();
        var previousVesselItems = RecipeIngredientCache.INSTANCE.copyValidVesselItems();
        RecipeIngredientCache.INSTANCE.setState(
                Map.of(
                        ModRecipeTypes.SOULIUM_SPAWNER,
                        Map.of(Items.ROTTEN_FLESH, List.of(Ingredient.of(Items.ROTTEN_FLESH)))
                ),
                Set.of()
        );

        try {
            var position = helper.absolutePos(BlockPos.ZERO);
            var machine = new SouliumSpawnerTileEntity(
                    position,
                    ModBlocks.SOULIUM_SPAWNER.defaultBlockState()
            );
            machine.setLevel(helper.getLevel());
            var storage = findStorage(helper, position, machine.getBlockState(), machine, Direction.UP);
            require(storage != null, "the soulium spawner did not expose top storage");

            try (var transaction = Transaction.openOuter()) {
                long inserted = storage.insert(
                        ItemVariant.of(Items.ROTTEN_FLESH),
                        600,
                        transaction
                );
                require(inserted == 512,
                        "the soulium spawner did not enforce its 512 item input limit");
                transaction.commit();
            }

            require(machine.getInventory().getAmountAsLong(0) == 512,
                    "the committed amount did not match the slot limit");
        } finally {
            RecipeIngredientCache.INSTANCE.setState(previousCaches, previousVesselItems);
        }
        helper.succeed();
    }

    @GameTest
    @SuppressWarnings("unchecked")
    public void detachedSpawnerMenuUsesPlayerLevelForFuelPlacementAndQuickMove(GameTestHelper helper) {
        var relativePosition = new BlockPos(0, 1, 0);
        helper.setBlock(relativePosition, ModBlocks.SOULIUM_SPAWNER);
        var position = helper.absolutePos(relativePosition);
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);

        var type = (ExtendedMenuType<SouliumSpawnerContainer, BlockPos>) ModMenuTypes.SOULIUM_SPAWNER;
        var menu = type.create(1, player.getInventory(), position);
        require(menu.getSlot(2).mayPlace(Items.COAL.getDefaultInstance()),
                "the detached spawner menu rejected valid fuel");

        player.getInventory().setItem(9, Items.COAL.getDefaultInstance());
        var moved = menu.quickMoveStack(player, 3);
        require(!moved.isEmpty(), "quick-moving fuel into the detached spawner menu failed");
        require(menu.getSlot(2).getItem().is(Items.COAL),
                "quick-moved fuel did not reach the spawner fuel slot");
        helper.succeed();
    }

    @GameTest
    public void abortedAndNestedTransactionsRestoreItems(GameTestHelper helper) {
        var inventory = CItemStacksHandler.create(1);
        var stone = ItemVariant.of(Items.STONE);

        try (var outer = Transaction.openOuter()) {
            inventory.insert(0, stone, 5, outer);
        }
        require(inventory.getAmountAsLong(0) == 0, "an aborted outer transaction retained items");

        try (var outer = Transaction.openOuter()) {
            inventory.insert(0, stone, 2, outer);
            try (var nested = outer.openNested()) {
                inventory.insert(0, stone, 3, nested);
            }
            require(inventory.getAmountAsLong(0) == 2, "an aborted nested transaction retained items");
            outer.commit();
        }

        require(inventory.getAmountAsLong(0) == 2, "the committed parent mutation was lost");
        helper.succeed();
    }

    @GameTest
    public void committedMultiStepMutationNotifiesExactlyOnce(GameTestHelper helper) {
        var notifications = new AtomicInteger();
        var inventory = CItemStacksHandler.create(1, (_, _) -> notifications.incrementAndGet(), _ -> {});
        var stone = ItemVariant.of(Items.STONE);

        try (var outer = Transaction.openOuter()) {
            inventory.insert(0, stone, 2, outer);
            try (var nested = outer.openNested()) {
                inventory.insert(0, stone, 3, nested);
                nested.commit();
            }
            require(notifications.get() == 0, "a notification escaped before the outer commit");
            outer.commit();
        }

        require(notifications.get() == 1, "the committed mutation did not notify exactly once");
        require(inventory.getAmountAsLong(0) == 5, "the committed nested amount was not retained");
        helper.succeed();
    }

    @GameTest
    public void energyCapacityCommitAndRollbackStayTransactional(GameTestHelper helper) {
        var notifications = new AtomicInteger();
        var energy = new CEnergyStorage(100, _ -> notifications.incrementAndGet());

        try (var outer = Transaction.openOuter()) {
            require(energy.insert(120, outer) == 100, "energy insertion exceeded capacity");
            outer.commit();
        }
        require(energy.getAmount() == 100, "committed energy was not retained");
        require(notifications.get() == 1, "committed energy did not notify exactly once");

        try (var outer = Transaction.openOuter()) {
            require(energy.extract(40, outer) == 40, "energy extraction returned the wrong amount");
        }
        require(energy.getAmount() == 100, "aborted energy extraction changed stored energy");
        require(notifications.get() == 1, "aborted energy extraction emitted a notification");
        helper.succeed();
    }

    private static SlottedStorage<ItemVariant> findStorage(
            GameTestHelper helper,
            BlockPos position,
            BlockState state,
            BlockEntity machine,
            Direction side
    ) {
        var storage = ItemStorage.SIDED.find(
                helper.getLevel(),
                position,
                state,
                machine,
                side
        );
        return storage instanceof SlottedStorage<ItemVariant> slotted ? slotted : null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
