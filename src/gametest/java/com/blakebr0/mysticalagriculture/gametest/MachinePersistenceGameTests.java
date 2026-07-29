package com.blakebr0.mysticalagriculture.gametest;

import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeTier;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceVesselTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachinePersistenceGameTests {
    @GameTest
    public void poweredProcessorRoundTripsInventoryAndEnergy(GameTestHelper helper) {
        var original = new ReprocessorTileEntity(BlockPos.ZERO, ModBlocks.REPROCESSOR.defaultBlockState());
        original.getInventory().set(0, ItemVariant.of(Items.WHEAT_SEEDS), 3);
        original.getUpgradeInventory().setStack(ModItems.SUPREMIUM_UPGRADE.getDefaultInstance());
        try (var transaction = Transaction.openOuter()) {
            original.getEnergy().insert(1_250, transaction);
            transaction.commit();
        }

        var loaded = roundTrip(original, ReprocessorTileEntity.class, helper);

        require(loaded.getInventory().getResource(0).isOf(Items.WHEAT_SEEDS), "processor input item was lost");
        require(loaded.getInventory().getAmountAsLong(0) == 3, "processor input count was lost");
        require(loaded.getEnergy().getAmount() == 1_250, "processor energy was lost");
        require(loaded.getMachineTier() == MachineUpgradeTier.SUPREMIUM, "processor upgrade tier was lost");
        helper.succeed();
    }

    @GameTest
    public void awakeningAltarRoundTripsInventoryAndActiveState(GameTestHelper helper) {
        var original = new AwakeningAltarTileEntity(BlockPos.ZERO, ModBlocks.AWAKENING_ALTAR.defaultBlockState());
        original.getInventory().set(0, ItemVariant.of(Items.DIAMOND), 1);
        original.activate();

        var loaded = roundTrip(original, AwakeningAltarTileEntity.class, helper);

        require(loaded.getInventory().getResource(0).isOf(Items.DIAMOND), "altar input was lost");
        require(loaded.isActive(), "altar active state was lost");
        helper.succeed();
    }

    @GameTest
    public void essenceVesselRoundTripsExtendedStackContents(GameTestHelper helper) {
        var original = new EssenceVesselTileEntity(BlockPos.ZERO, ModBlocks.ESSENCE_VESSEL.defaultBlockState());
        original.getInventory().set(0, ItemVariant.of(Items.REDSTONE), 40);

        var loaded = roundTrip(original, EssenceVesselTileEntity.class, helper);

        require(loaded.getInventory().getResource(0).isOf(Items.REDSTONE), "vessel item was lost");
        require(loaded.getInventory().getAmountAsLong(0) == 40, "vessel count was lost");
        helper.succeed();
    }

    @GameTest
    public void souliumSpawnerRoundTripsInputAndEnergy(GameTestHelper helper) {
        var original = new SouliumSpawnerTileEntity(BlockPos.ZERO, ModBlocks.SOULIUM_SPAWNER.defaultBlockState());
        original.getInventory().set(0, ItemVariant.of(Items.ROTTEN_FLESH), 128);
        try (var transaction = Transaction.openOuter()) {
            original.getEnergy().insert(4_000, transaction);
            transaction.commit();
        }

        var tag = original.saveWithFullMetadata(helper.getLevel().registryAccess());
        var expectedRecipeId = Identifier.parse("mysticalagriculture:gametest_spawner");
        tag.putString("recipe_id", expectedRecipeId.toString());
        var loaded = load(original, tag, SouliumSpawnerTileEntity.class, helper);

        require(loaded.getInventory().getResource(0).isOf(Items.ROTTEN_FLESH), "spawner input was lost");
        require(loaded.getInventory().getAmountAsLong(0) == 128, "spawner input count was lost");
        require(loaded.getEnergy().getAmount() == 4_000, "spawner energy was lost");
        require(expectedRecipeId.equals(loaded.getActiveRecipeId()), "spawner recipe ID was lost");
        helper.succeed();
    }

    @GameTest
    public void harvesterRoundTripsCurrentAndLegacyFuelItemValueKeys(GameTestHelper helper) {
        var original = new HarvesterTileEntity(BlockPos.ZERO, ModBlocks.HARVESTER.defaultBlockState());
        original.setLevel(helper.getLevel());
        original.getInventory().set(0, ItemVariant.of(Items.COAL), 1);
        HarvesterTileEntity.tick(helper.getLevel(), BlockPos.ZERO, original.getBlockState(), original);
        var expectedFuelItemValue = original.getFuelItemValue();

        require(expectedFuelItemValue > 0, "harvester did not establish a fuel item value");
        var loaded = roundTrip(original, HarvesterTileEntity.class, helper);
        require(loaded.getFuelItemValue() == expectedFuelItemValue,
                "harvester lost the current fuel_item_value field");

        var legacyTag = original.saveWithFullMetadata(helper.getLevel().registryAccess());
        legacyTag.remove("fuel_item_value");
        legacyTag.putInt("fuel_left_value", expectedFuelItemValue);
        var legacyLoaded = load(original, legacyTag, HarvesterTileEntity.class, helper);
        require(legacyLoaded.getFuelItemValue() == expectedFuelItemValue,
                "harvester did not read the legacy fuel_left_value field");
        helper.succeed();
    }

    private static <T extends BlockEntity> T roundTrip(
            T original,
            Class<T> expectedType,
            GameTestHelper helper
    ) {
        var tag = original.saveWithFullMetadata(helper.getLevel().registryAccess());
        return load(original, tag, expectedType, helper);
    }

    private static <T extends BlockEntity> T load(
            T original,
            net.minecraft.nbt.CompoundTag tag,
            Class<T> expectedType,
            GameTestHelper helper
    ) {
        var loaded = BlockEntity.loadStatic(
                original.getBlockPos(),
                original.getBlockState(),
                tag,
                helper.getLevel().registryAccess()
        );
        require(expectedType.isInstance(loaded), "block entity round trip returned the wrong type");
        return expectedType.cast(loaded);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
