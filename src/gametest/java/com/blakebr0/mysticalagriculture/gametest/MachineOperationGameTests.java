package com.blakebr0.mysticalagriculture.gametest;

import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.container.EnchanterContainer;
import com.blakebr0.mysticalagriculture.container.TinkeringTableContainer;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.lib.ModAugments;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningPedestalTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EnchanterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceVesselTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.InfusionAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.InfusionPedestalTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.TinkeringTableTileEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class MachineOperationGameTests {
    @GameTest
    public void poweredProcessorsExecuteRealRecipesAcrossSaveReload(GameTestHelper helper) {
        var furnace = place(helper, new BlockPos(1, 1, 1), ModBlocks.FURNACE, EssenceFurnaceTileEntity.class);
        furnace.getInventory().set(0, ItemVariant.of(Items.RAW_IRON), 1);
        primeUpgradedMachine(furnace, EssenceFurnaceTileEntity.FUEL_CAPACITY);
        EssenceFurnaceTileEntity.tick(
                helper.getLevel(),
                furnace.getBlockPos(),
                furnace.getBlockState(),
                furnace
        );
        require(furnace.getOperationTime() < EssenceFurnaceTileEntity.OPERATION_TIME,
                "furnace upgrade did not reduce operation time");
        require(furnace.getProgress() == 1, "furnace did not start its smelting recipe");
        furnace = reload(furnace, EssenceFurnaceTileEntity.class, helper);
        require(furnace.getProgress() == 1, "furnace progress did not survive save/reload");
        tickFurnace(helper, furnace, furnace.getOperationTime() - 1);
        requireInventory(furnace, 0, Items.AIR, 0, "furnace did not consume raw iron");
        requireInventory(furnace, 2, Items.IRON_INGOT, 1, "furnace did not produce an iron ingot");

        var reprocessor = place(helper, new BlockPos(2, 1, 1), ModBlocks.REPROCESSOR, ReprocessorTileEntity.class);
        reprocessor.getInventory().set(0, ItemVariant.of(ModCrops.INFERIUM.getSeedsItem()), 1);
        primeUpgradedMachine(reprocessor, ReprocessorTileEntity.FUEL_CAPACITY);
        ReprocessorTileEntity.tick(
                helper.getLevel(),
                reprocessor.getBlockPos(),
                reprocessor.getBlockState(),
                reprocessor
        );
        require(reprocessor.getOperationTime() < ReprocessorTileEntity.OPERATION_TIME,
                "reprocessor upgrade did not reduce operation time");
        require(reprocessor.getProgress() == 1, "reprocessor did not start its seed recipe");
        reprocessor = reload(reprocessor, ReprocessorTileEntity.class, helper);
        require(reprocessor.getProgress() == 1, "reprocessor progress did not survive save/reload");
        tickReprocessor(helper, reprocessor, reprocessor.getOperationTime() - 1);
        requireInventory(reprocessor, 0, Items.AIR, 0, "reprocessor did not consume its seed");
        requireInventory(reprocessor, 2, ModItems.INFERIUM_ESSENCE, 2,
                "reprocessor did not produce two inferium essence");

        var extractor = place(helper, new BlockPos(3, 1, 1), ModBlocks.SOUL_EXTRACTOR, SoulExtractorTileEntity.class);
        extractor.getInventory().set(0, ItemVariant.of(Items.ROTTEN_FLESH), 1);
        extractor.getInventory().set(2, ItemVariant.of(ModItems.SOUL_JAR), 1);
        primeUpgradedMachine(extractor, SoulExtractorTileEntity.FUEL_CAPACITY);
        SoulExtractorTileEntity.tick(
                helper.getLevel(),
                extractor.getBlockPos(),
                extractor.getBlockState(),
                extractor
        );
        require(extractor.getOperationTime() < SoulExtractorTileEntity.OPERATION_TIME,
                "soul extractor upgrade did not reduce operation time");
        require(extractor.getProgress() == 1, "soul extractor did not start its extraction recipe");
        extractor = reload(extractor, SoulExtractorTileEntity.class, helper);
        require(extractor.getProgress() == 1, "soul extractor progress did not survive save/reload");
        tickSoulExtractor(helper, extractor, extractor.getOperationTime() - 1);
        requireInventory(extractor, 0, Items.AIR, 0, "soul extractor did not consume rotten flesh");
        var soulJar = extractor.getInventory().getResource(2)
                .toStack(extractor.getInventory().getAmountAsInt(2));
        require(soulJar.is(ModItems.SOUL_JAR), "soul extractor replaced the soul jar with the wrong item");
        require(MobSoulUtils.getSouls(soulJar) == 0.5D,
                "soul extractor produced " + MobSoulUtils.getSouls(soulJar) + " souls instead of 0.5");

        var infuser = place(helper, new BlockPos(4, 1, 1), ModBlocks.ORE_INFUSER, OreInfuserTileEntity.class);
        infuser.getInventory().set(0, ItemVariant.of(Items.STONE), 1);
        infuser.getInventory().set(1, ItemVariant.of(ModCrops.IRON.getEssenceItem()), 4);
        primeUpgradedMachine(infuser, OreInfuserTileEntity.FUEL_CAPACITY);
        OreInfuserTileEntity.tick(
                helper.getLevel(),
                infuser.getBlockPos(),
                infuser.getBlockState(),
                infuser
        );
        require(infuser.getOperationTime() < OreInfuserTileEntity.OPERATION_TIME,
                "ore infuser upgrade did not reduce operation time");
        require(infuser.getProgress() == 1, "ore infuser did not start its infusion recipe");
        infuser = reload(infuser, OreInfuserTileEntity.class, helper);
        require(infuser.getProgress() == 1, "ore infuser progress did not survive save/reload");
        tickOreInfuser(helper, infuser, infuser.getOperationTime() - 1);
        requireInventory(infuser, 0, Items.AIR, 0, "ore infuser did not consume stone");
        requireInventory(infuser, 1, Items.AIR, 0, "ore infuser did not consume four iron essence");
        requireInventory(infuser, 3, Items.IRON_ORE, 1, "ore infuser did not produce iron ore");

        helper.succeed();
    }

    @GameTest
    public void poweredMachineFacesAndEnergyExecuteCommittedTransactions(GameTestHelper helper) {
        var position = new BlockPos(1, 1, 1);
        var furnace = place(helper, position, ModBlocks.FURNACE, EssenceFurnaceTileEntity.class);
        var absolute = furnace.getBlockPos();
        var top = ItemStorage.SIDED.find(
                helper.getLevel(),
                absolute,
                furnace.getBlockState(),
                furnace,
                Direction.UP
        );

        require(top instanceof SlottedStorage<ItemVariant>, "furnace top face did not expose slotted storage");
        try (var transaction = Transaction.openOuter()) {
            require(top.insert(ItemVariant.of(Items.RAW_IRON), 1, transaction) == 1,
                    "furnace rejected committed top-face input");
            transaction.commit();
        }
        try (var transaction = Transaction.openOuter()) {
            require(furnace.getEnergy().insert(4_000, transaction) == 4_000,
                    "furnace energy storage rejected a valid committed insertion");
            transaction.commit();
        }

        tickFurnace(helper, furnace, EssenceFurnaceTileEntity.OPERATION_TIME);

        requireInventory(furnace, 2, Items.IRON_INGOT, 1,
                "sided input plus committed energy did not produce furnace output");
        require(furnace.getEnergy().getAmount() == 0,
                "furnace consumed an unexpected amount of energy for one recipe");
        helper.succeed();
    }

    @GameTest
    public void harvesterHarvestsMatureCropAfterProgressReloadAndUpgradeScaling(GameTestHelper helper) {
        var position = new BlockPos(4, 2, 6);
        helper.setBlock(position, ModBlocks.HARVESTER.defaultBlockState());
        var harvester = blockEntity(helper, position, HarvesterTileEntity.class);
        harvester.getUpgradeInventory().setStack(ModItems.INFERIUM_UPGRADE.getDefaultInstance());
        fillEnergy(harvester.getEnergy(), 20_000);

        HarvesterTileEntity.tick(
                helper.getLevel(),
                harvester.getBlockPos(),
                harvester.getBlockState(),
                harvester
        );
        require(harvester.getOperationTime() < HarvesterTileEntity.OPERATION_TIME,
                "harvester upgrade did not reduce operation time");

        var range = 2;
        var center = position.relative(Direction.NORTH, range + 1);
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                var cropPos = center.offset(x, 0, -z);
                helper.setBlock(cropPos.below(), net.minecraft.world.level.block.Blocks.FARMLAND);
                helper.setBlock(cropPos, net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState()
                        .setValue(CropBlock.AGE, CropBlock.MAX_AGE));
            }
        }

        harvester = reload(harvester, HarvesterTileEntity.class, helper);
        require(harvester.getProgress() == 1, "harvester progress did not survive save/reload");
        for (int i = 0; i <= harvester.getOperationTime(); i++) {
            HarvesterTileEntity.tick(
                    helper.getLevel(),
                    harvester.getBlockPos(),
                    helper.getLevel().getBlockState(harvester.getBlockPos()),
                    harvester
            );
        }

        var firstCrop = center.offset(-range, 0, range);
        var firstState = helper.getBlockState(firstCrop);
        require(firstState.is(net.minecraft.world.level.block.Blocks.WHEAT)
                        && firstState.getValue(CropBlock.AGE) == 0,
                "harvester did not reset the first mature crop");
        var outputAmount = 0L;
        for (int slot = 1; slot < harvester.getInventory().getContainerSize(); slot++) {
            outputAmount += harvester.getInventory().getAmountAsLong(slot);
        }
        require(outputAmount > 0, "harvester did not retain any harvested output");
        require(harvester.getEnergy().getAmount() < 20_000,
                "harvester operation did not consume energy");
        helper.succeed();
    }

    @GameTest
    public void enchanterAndTinkeringTableExecuteRealMenuOperations(GameTestHelper helper) {
        var player = FakePlayer.get(helper.getLevel());

        var enchanter = place(helper, new BlockPos(1, 1, 1), ModBlocks.ENCHANTER, EnchanterTileEntity.class);
        enchanter.getInventory().set(0, ItemVariant.of(Items.OBSIDIAN), 12);
        enchanter.getInventory().set(1, ItemVariant.of(ModCrops.EXPERIENCE.getEssenceItem()), 64);
        enchanter.getInventory().set(2, ItemVariant.of(Items.BOOK), 1);
        var enchanterMenu = new EnchanterContainer(
                1,
                player.getInventory(),
                enchanter.getInventory(),
                enchanter.getBlockPos()
        );
        enchanterMenu.slotsChanged(enchanter.getInventory().toRecipeInventory());
        var enchantedBook = enchanterMenu.getSlot(3).getItem().copy();
        require(enchantedBook.is(Items.ENCHANTED_BOOK), "enchanter did not create an enchanted book");
        require(!EnchantmentHelper.getEnchantmentsForCrafting(enchantedBook).isEmpty(),
                "enchanter output contains no enchantment");
        enchanterMenu.getSlot(3).onTake(player, enchantedBook);
        requireInventory(enchanter, 0, Items.AIR, 0, "enchanter did not consume 12 obsidian");
        requireInventory(enchanter, 1, Items.AIR, 0, "enchanter did not consume 64 experience essence");
        requireInventory(enchanter, 2, Items.AIR, 0, "enchanter did not consume its book");

        var table = place(helper, new BlockPos(3, 1, 1), ModBlocks.TINKERING_TABLE, TinkeringTableTileEntity.class);
        table.getInventory().set(0, ItemVariant.of(ModItems.SUPREMIUM_SWORD), 1);
        table.getInventory().set(1, ItemVariant.of(ModAugments.STRENGTH_III.getItem()), 1);
        var tableMenu = new TinkeringTableContainer(
                2,
                player.getInventory(),
                table.getInventory(),
                table.getBlockPos()
        );
        tableMenu.slotsChanged(table.getInventory().toRecipeInventory());
        var tinkeredSword = table.getInventory().getResource(0)
                .toStack(table.getInventory().getAmountAsInt(0));
        require(AugmentUtils.getAugment(tinkeredSword, 0) == ModAugments.STRENGTH_III,
                "tinkering table did not install the strength III augment");
        helper.succeed();
    }

    @GameTest
    public void infusionAndAwakeningAltarsConsumeMultiblocksAcrossProgressReload(GameTestHelper helper) {
        var infusion = place(
                helper,
                new BlockPos(4, 1, 4),
                ModBlocks.INFUSION_ALTAR,
                InfusionAltarTileEntity.class
        );
        var infusionIngredients = List.of(
                Items.DIAMOND,
                ModItems.TERTIUM_ESSENCE,
                Blocks.WOOL.white().asItem(),
                ModItems.TERTIUM_ESSENCE,
                Items.DIAMOND,
                ModItems.TERTIUM_ESSENCE,
                Blocks.WOOL.white().asItem(),
                ModItems.TERTIUM_ESSENCE
        );
        var infusionPedestals = placeInfusionPedestals(helper, infusion);
        infusion.getInventory().set(0, ItemVariant.of(ModItems.BLANK_AUGMENT), 1);
        for (int i = 0; i < infusionPedestals.size(); i++) {
            infusionPedestals.get(i).getInventory().set(0, ItemVariant.of(infusionIngredients.get(i)), 1);
        }
        infusion.activate();
        tickInfusion(helper, infusion, 40);
        require(infusion.getActiveRecipeId() != null, "infusion altar did not start a real recipe");
        infusion = reload(infusion, InfusionAltarTileEntity.class, helper);
        tickInfusion(helper, infusion, 60);
        requireInventory(infusion, 0, Items.AIR, 0, "infusion altar did not consume its center input");
        requireInventory(infusion, 1, ModAugments.NO_FALL_DAMAGE.getItem(), 1,
                "infusion altar did not produce the no-fall augment");
        for (var pedestal : infusionPedestals) {
            requireInventory(pedestal, 0, Items.AIR, 0,
                    "infusion altar did not consume a pedestal ingredient");
        }

        var awakening = place(
                helper,
                new BlockPos(4, 1, 4),
                ModBlocks.AWAKENING_ALTAR,
                AwakeningAltarTileEntity.class
        );
        var awakeningPositions = awakening.getPedestalPositions();
        var awakeningPedestals = awakeningPositions.subList(0, 4)
                .stream()
                .map(pos -> placeAbsolute(helper, pos, ModBlocks.AWAKENING_PEDESTAL, AwakeningPedestalTileEntity.class))
                .toList();
        var vessels = awakeningPositions.subList(4, 8)
                .stream()
                .map(pos -> placeAbsolute(helper, pos, ModBlocks.ESSENCE_VESSEL, EssenceVesselTileEntity.class))
                .toList();
        awakening.getInventory().set(0, ItemVariant.of(ModBlocks.SUPREMIUM_BLOCK.asItem()), 1);
        for (var pedestal : awakeningPedestals) {
            pedestal.getInventory().set(0, ItemVariant.of(ModItems.COGNIZANT_DUST), 1);
        }
        var elementalEssences = List.of(
                ModCrops.AIR.getEssenceItem(),
                ModCrops.EARTH.getEssenceItem(),
                ModCrops.WATER.getEssenceItem(),
                ModCrops.FIRE.getEssenceItem()
        );
        for (int i = 0; i < vessels.size(); i++) {
            vessels.get(i).getInventory().set(0, ItemVariant.of(elementalEssences.get(i)), 10);
        }
        awakening.activate();
        tickAwakening(helper, awakening, 40);
        require(awakening.getActiveRecipeId() != null, "awakening altar did not start a real recipe");
        awakening = reload(awakening, AwakeningAltarTileEntity.class, helper);
        tickAwakening(helper, awakening, 60);
        requireInventory(awakening, 0, Items.AIR, 0, "awakening altar did not consume its center input");
        requireInventory(awakening, 1, ModBlocks.AWAKENED_SUPREMIUM_BLOCK.asItem(), 1,
                "awakening altar did not produce awakened supremium");
        for (var pedestal : awakeningPedestals) {
            requireInventory(pedestal, 0, Items.AIR, 0,
                    "awakening altar did not consume cognizant dust");
        }
        for (var vessel : vessels) {
            requireInventory(vessel, 0, Items.AIR, 0,
                    "awakening altar did not consume required elemental essence");
        }
        helper.succeed();
    }

    @GameTest
    public void souliumSpawnerConsumesRecipeAndCreatesEntityAcrossProgressReload(GameTestHelper helper) {
        var spawner = place(
                helper,
                new BlockPos(4, 2, 4),
                ModBlocks.SOULIUM_SPAWNER,
                SouliumSpawnerTileEntity.class
        );
        spawner.getInventory().set(0, ItemVariant.of(ModCrops.ZOMBIE.getEssenceItem()), 16);
        primeUpgradedMachine(spawner, SouliumSpawnerTileEntity.FUEL_CAPACITY);
        var entitiesBefore = helper.getLevel().getEntities(EntityTypes.ZOMBIE, _ -> true).size()
                + helper.getLevel().getEntities(EntityTypes.ZOMBIE_VILLAGER, _ -> true).size()
                + helper.getLevel().getEntities(EntityTypes.HUSK, _ -> true).size();

        SouliumSpawnerTileEntity.tick(
                helper.getLevel(),
                spawner.getBlockPos(),
                spawner.getBlockState(),
                spawner
        );
        require(spawner.getOperationTime() < SouliumSpawnerTileEntity.OPERATION_TIME,
                "soulium spawner upgrade did not reduce operation time");
        require(spawner.getProgress() == 1, "soulium spawner did not start its entity recipe");
        spawner = reload(spawner, SouliumSpawnerTileEntity.class, helper);
        require(spawner.getProgress() == 1, "soulium spawner progress did not survive save/reload");
        for (int i = 0; i < spawner.getOperationTime() - 1; i++) {
            SouliumSpawnerTileEntity.tick(
                    helper.getLevel(),
                    spawner.getBlockPos(),
                    helper.getLevel().getBlockState(spawner.getBlockPos()),
                    spawner
            );
        }

        requireInventory(spawner, 0, Items.AIR, 0,
                "soulium spawner did not consume 16 zombie essence");
        var entitiesAfter = helper.getLevel().getEntities(EntityTypes.ZOMBIE, _ -> true).size()
                + helper.getLevel().getEntities(EntityTypes.ZOMBIE_VILLAGER, _ -> true).size()
                + helper.getLevel().getEntities(EntityTypes.HUSK, _ -> true).size();
        require(entitiesAfter == entitiesBefore + 1,
                "soulium spawner did not create exactly one configured zombie-family entity");
        helper.succeed();
    }

    private static void primeUpgradedMachine(EssenceFurnaceTileEntity machine, int baseCapacity) {
        machine.getUpgradeInventory().setStack(ModItems.INFERIUM_UPGRADE.getDefaultInstance());
        fillEnergy(machine.getEnergy(), baseCapacity);
    }

    private static void primeUpgradedMachine(ReprocessorTileEntity machine, int baseCapacity) {
        machine.getUpgradeInventory().setStack(ModItems.INFERIUM_UPGRADE.getDefaultInstance());
        fillEnergy(machine.getEnergy(), baseCapacity);
    }

    private static void primeUpgradedMachine(SoulExtractorTileEntity machine, int baseCapacity) {
        machine.getUpgradeInventory().setStack(ModItems.INFERIUM_UPGRADE.getDefaultInstance());
        fillEnergy(machine.getEnergy(), baseCapacity);
    }

    private static void primeUpgradedMachine(OreInfuserTileEntity machine, int baseCapacity) {
        machine.getUpgradeInventory().setStack(ModItems.INFERIUM_UPGRADE.getDefaultInstance());
        fillEnergy(machine.getEnergy(), baseCapacity);
    }

    private static void primeUpgradedMachine(SouliumSpawnerTileEntity machine, int baseCapacity) {
        machine.getUpgradeInventory().setStack(ModItems.INFERIUM_UPGRADE.getDefaultInstance());
        fillEnergy(machine.getEnergy(), baseCapacity);
    }

    private static void fillEnergy(com.blakebr0.cucumber.energy.CEnergyStorage energy, long amount) {
        try (var transaction = Transaction.openOuter()) {
            require(energy.insert(amount, transaction) == amount, "machine rejected test energy");
            transaction.commit();
        }
    }

    private static void tickFurnace(GameTestHelper helper, EssenceFurnaceTileEntity machine, int count) {
        for (int i = 0; i < count; i++) {
            EssenceFurnaceTileEntity.tick(
                    helper.getLevel(),
                    machine.getBlockPos(),
                    helper.getLevel().getBlockState(machine.getBlockPos()),
                    machine
            );
        }
    }

    private static void tickReprocessor(GameTestHelper helper, ReprocessorTileEntity machine, int count) {
        for (int i = 0; i < count; i++) {
            ReprocessorTileEntity.tick(
                    helper.getLevel(),
                    machine.getBlockPos(),
                    helper.getLevel().getBlockState(machine.getBlockPos()),
                    machine
            );
        }
    }

    private static void tickSoulExtractor(GameTestHelper helper, SoulExtractorTileEntity machine, int count) {
        for (int i = 0; i < count; i++) {
            SoulExtractorTileEntity.tick(
                    helper.getLevel(),
                    machine.getBlockPos(),
                    helper.getLevel().getBlockState(machine.getBlockPos()),
                    machine
            );
        }
    }

    private static void tickOreInfuser(GameTestHelper helper, OreInfuserTileEntity machine, int count) {
        for (int i = 0; i < count; i++) {
            OreInfuserTileEntity.tick(
                    helper.getLevel(),
                    machine.getBlockPos(),
                    helper.getLevel().getBlockState(machine.getBlockPos()),
                    machine
            );
        }
    }

    private static void tickInfusion(GameTestHelper helper, InfusionAltarTileEntity altar, int count) {
        for (int i = 0; i < count; i++) {
            InfusionAltarTileEntity.tick(
                    helper.getLevel(),
                    altar.getBlockPos(),
                    helper.getLevel().getBlockState(altar.getBlockPos()),
                    altar
            );
        }
    }

    private static void tickAwakening(GameTestHelper helper, AwakeningAltarTileEntity altar, int count) {
        for (int i = 0; i < count; i++) {
            AwakeningAltarTileEntity.tick(
                    helper.getLevel(),
                    altar.getBlockPos(),
                    helper.getLevel().getBlockState(altar.getBlockPos()),
                    altar
            );
        }
    }

    private static List<InfusionPedestalTileEntity> placeInfusionPedestals(
            GameTestHelper helper,
            InfusionAltarTileEntity altar
    ) {
        return altar.getPedestalPositions()
                .stream()
                .map(pos -> placeAbsolute(
                        helper,
                        pos,
                        ModBlocks.INFUSION_PEDESTAL,
                        InfusionPedestalTileEntity.class
                ))
                .toList();
    }

    private static <T extends BlockEntity> T place(
            GameTestHelper helper,
            BlockPos relativePosition,
            Block block,
            Class<T> type
    ) {
        helper.setBlock(relativePosition, block);
        return blockEntity(helper, relativePosition, type);
    }

    private static <T extends BlockEntity> T placeAbsolute(
            GameTestHelper helper,
            BlockPos absolutePosition,
            Block block,
            Class<T> type
    ) {
        helper.getLevel().setBlock(absolutePosition, block.defaultBlockState(), 3);
        var blockEntity = helper.getLevel().getBlockEntity(absolutePosition);
        require(type.isInstance(blockEntity), "placed block did not create " + type.getSimpleName());
        return type.cast(blockEntity);
    }

    private static <T extends BlockEntity> T blockEntity(
            GameTestHelper helper,
            BlockPos relativePosition,
            Class<T> type
    ) {
        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePosition));
        require(type.isInstance(blockEntity), "placed block did not create " + type.getSimpleName());
        return type.cast(blockEntity);
    }

    private static <T extends BlockEntity> T reload(
            T original,
            Class<T> type,
            GameTestHelper helper
    ) {
        var tag = original.saveWithFullMetadata(helper.getLevel().registryAccess());
        var loaded = BlockEntity.loadStatic(
                original.getBlockPos(),
                original.getBlockState(),
                tag,
                helper.getLevel().registryAccess()
        );
        require(type.isInstance(loaded), "save/reload returned the wrong block entity type");
        var result = type.cast(loaded);
        result.setLevel(helper.getLevel());
        return result;
    }

    private static void requireInventory(
            com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity machine,
            int slot,
            Item item,
            long amount,
            String message
    ) {
        var inventory = machine.getInventory();
        var actualItem = inventory.getResource(slot).getItem();
        var actualAmount = inventory.getAmountAsLong(slot);
        var itemMatches = amount == 0 ? inventory.getResource(slot).isBlank() : actualItem == item;
        require(itemMatches && actualAmount == amount,
                message + "; found " + actualItem + " x" + actualAmount);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
