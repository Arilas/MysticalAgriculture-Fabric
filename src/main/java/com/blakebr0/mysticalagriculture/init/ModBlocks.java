package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.cucumber.block.BaseBlock;
import com.blakebr0.cucumber.block.BaseGlassBlock;
import com.blakebr0.cucumber.block.BaseOreBlock;
import com.blakebr0.cucumber.block.BaseSlabBlock;
import com.blakebr0.cucumber.block.BaseStairsBlock;
import com.blakebr0.cucumber.block.BaseWallBlock;
import com.blakebr0.cucumber.item.BaseBlockItem;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.block.AwakeningAltarBlock;
import com.blakebr0.mysticalagriculture.block.AwakeningPedestalBlock;
import com.blakebr0.mysticalagriculture.block.EnchanterBlock;
import com.blakebr0.mysticalagriculture.block.EssenceFurnaceBlock;
import com.blakebr0.mysticalagriculture.block.EssenceVesselBlock;
import com.blakebr0.mysticalagriculture.block.GrowthAcceleratorBlock;
import com.blakebr0.mysticalagriculture.block.HarvesterBlock;
import com.blakebr0.mysticalagriculture.block.InferiumCropBlock;
import com.blakebr0.mysticalagriculture.block.InfusedFarmlandBlock;
import com.blakebr0.mysticalagriculture.block.InfusionAltarBlock;
import com.blakebr0.mysticalagriculture.block.InfusionPedestalBlock;
import com.blakebr0.mysticalagriculture.block.MysticalCropBlock;
import com.blakebr0.mysticalagriculture.block.OreInfuserBlock;
import com.blakebr0.mysticalagriculture.block.ReprocessorBlock;
import com.blakebr0.mysticalagriculture.block.SoulExtractorBlock;
import com.blakebr0.mysticalagriculture.block.SouliumSpawnerBlock;
import com.blakebr0.mysticalagriculture.block.TinkeringTableBlock;
import com.blakebr0.mysticalagriculture.block.WitherproofBlock;
import com.blakebr0.mysticalagriculture.block.WitherproofGlassBlock;
import com.blakebr0.mysticalagriculture.lib.ModCorePlugin;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.blakebr0.mysticalagriculture.registry.DirectRegistryRegistrar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ModBlocks {
    private static final Map<Identifier, Block> ENTRIES = new LinkedHashMap<>();
    private static final Map<Identifier, Function<Identifier, BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();
    private static final DirectRegistryRegistrar<Block> BLOCK_REGISTRAR =
            new DirectRegistryRegistrar<>(BuiltInRegistries.BLOCK, "block");
    private static final DirectRegistryRegistrar<Item> ITEM_REGISTRAR =
            new DirectRegistryRegistrar<>(BuiltInRegistries.ITEM, "item");

    public static final Block PROSPERITY_BLOCK = register("prosperity_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block INFERIUM_BLOCK = register("inferium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block PRUDENTIUM_BLOCK = register("prudentium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block TERTIUM_BLOCK = register("tertium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block IMPERIUM_BLOCK = register("imperium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 5.0F, true));
    public static final Block SUPREMIUM_BLOCK = register("supremium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block AWAKENED_SUPREMIUM_BLOCK = register("awakened_supremium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block SOULIUM_BLOCK = register("soulium_block", id -> new BaseBlock(id, SoundType.STONE, 4.0F, 6.0F, true));
    public static final Block PROSPERITY_INGOT_BLOCK = register("prosperity_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block INFERIUM_INGOT_BLOCK = register("inferium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block PRUDENTIUM_INGOT_BLOCK = register("prudentium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block TERTIUM_INGOT_BLOCK = register("tertium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block IMPERIUM_INGOT_BLOCK = register("imperium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block SUPREMIUM_INGOT_BLOCK = register("supremium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block AWAKENED_SUPREMIUM_INGOT_BLOCK = register("awakened_supremium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block SOULIUM_INGOT_BLOCK = register("soulium_ingot_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block PROSPERITY_GEMSTONE_BLOCK = register("prosperity_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block INFERIUM_GEMSTONE_BLOCK = register("inferium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block PRUDENTIUM_GEMSTONE_BLOCK = register("prudentium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block TERTIUM_GEMSTONE_BLOCK = register("tertium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block IMPERIUM_GEMSTONE_BLOCK = register("imperium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block SUPREMIUM_GEMSTONE_BLOCK = register("supremium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block AWAKENED_SUPREMIUM_GEMSTONE_BLOCK = register("awakened_supremium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block SOULIUM_GEMSTONE_BLOCK = register("soulium_gemstone_block", id -> new BaseBlock(id, SoundType.METAL, 5.0F, 6.0F, true));
    public static final Block INFERIUM_FARMLAND = register("inferium_farmland", id -> new InfusedFarmlandBlock(id, CropTier.ONE));
    public static final Block PRUDENTIUM_FARMLAND = register("prudentium_farmland", id -> new InfusedFarmlandBlock(id, CropTier.TWO));
    public static final Block TERTIUM_FARMLAND = register("tertium_farmland", id -> new InfusedFarmlandBlock(id, CropTier.THREE));
    public static final Block IMPERIUM_FARMLAND = register("imperium_farmland", id -> new InfusedFarmlandBlock(id, CropTier.FOUR));
    public static final Block SUPREMIUM_FARMLAND = register("supremium_farmland", id -> new InfusedFarmlandBlock(id, CropTier.FIVE));
    public static final Block AWAKENED_SUPREMIUM_FARMLAND = register("awakened_supremium_farmland", id -> new InfusedFarmlandBlock(id, ModCorePlugin.AWAKENED_SUPREMIUM_TIER));
    public static final Block INFERIUM_GROWTH_ACCELERATOR = register("inferium_growth_accelerator", id -> new GrowthAcceleratorBlock(id, 9, CropTier.ONE.getTextColor()));
    public static final Block PRUDENTIUM_GROWTH_ACCELERATOR = register("prudentium_growth_accelerator", id -> new GrowthAcceleratorBlock(id, 18, CropTier.TWO.getTextColor()));
    public static final Block TERTIUM_GROWTH_ACCELERATOR = register("tertium_growth_accelerator", id -> new GrowthAcceleratorBlock(id, 27, CropTier.THREE.getTextColor()));
    public static final Block IMPERIUM_GROWTH_ACCELERATOR = register("imperium_growth_accelerator", id -> new GrowthAcceleratorBlock(id, 36, CropTier.FOUR.getTextColor()));
    public static final Block SUPREMIUM_GROWTH_ACCELERATOR = register("supremium_growth_accelerator", id -> new GrowthAcceleratorBlock(id, 45, CropTier.FIVE.getTextColor()));
    public static final Block AWAKENED_SUPREMIUM_GROWTH_ACCELERATOR = register("awakened_supremium_growth_accelerator", id -> new GrowthAcceleratorBlock(id, 54, CropTier.FIVE.getTextColor()));
    public static final Block PROSPERITY_ORE = register("prosperity_ore", id -> new BaseOreBlock(id, SoundType.STONE, 3.0F, 3.0F, 2, 5));
    public static final Block DEEPSLATE_PROSPERITY_ORE = register("deepslate_prosperity_ore", id -> new BaseOreBlock(id, SoundType.DEEPSLATE, 4.5F, 3.0F, 2, 5));
    public static final Block INFERIUM_ORE = register("inferium_ore", id -> new BaseOreBlock(id, SoundType.STONE, 3.0F, 3.0F, 2, 5));
    public static final Block DEEPSLATE_INFERIUM_ORE = register("deepslate_inferium_ore", id -> new BaseOreBlock(id, SoundType.DEEPSLATE, 4.5F, 3.0F, 2, 5));
    public static final Block SOULIUM_ORE = register("soulium_ore", id -> new BaseOreBlock(id, SoundType.STONE, 3.0F, 3.0F, 3, 7));
    public static final Block SOULSTONE = register("soulstone", id -> new BaseBlock(id, SoundType.STONE, 1.5F, 6.0F, true));
    public static final Block SOULSTONE_COBBLE = register("soulstone_cobble", id -> new BaseBlock(id, SoundType.STONE, 2.0F, 6.0F, true));
    public static final Block SOULSTONE_BRICKS = register("soulstone_bricks", id -> new BaseBlock(id, SoundType.STONE, 1.5F, 6.0F, true));
    public static final Block SOULSTONE_CRACKED_BRICKS = register("soulstone_cracked_bricks", id -> new BaseBlock(id, SoundType.STONE, 1.5F, 6.0F, true));
    public static final Block SOULSTONE_CHISELED_BRICKS = register("soulstone_chiseled_bricks", id -> new BaseBlock(id, SoundType.STONE, 1.5F, 6.0F, true));
    public static final Block SOULSTONE_SMOOTH = register("soulstone_smooth", id -> new BaseBlock(id, SoundType.STONE, 1.5F, 6.0F, true));
    public static final Block SOUL_GLASS = register("soul_glass", id -> new BaseGlassBlock(id, SoundType.GLASS, 0.3F, 0.3F));
    public static final Block SOULSTONE_SLAB = register("soulstone_slab", id -> new BaseSlabBlock(id, () -> SOULSTONE));
    public static final Block SOULSTONE_COBBLE_SLAB = register("soulstone_cobble_slab", id -> new BaseSlabBlock(id, () -> SOULSTONE_COBBLE));
    public static final Block SOULSTONE_BRICKS_SLAB = register("soulstone_bricks_slab", id -> new BaseSlabBlock(id, () -> SOULSTONE_BRICKS));
    public static final Block SOULSTONE_SMOOTH_SLAB = register("soulstone_smooth_slab", id -> new BaseSlabBlock(id, () -> SOULSTONE_SMOOTH));
    public static final Block SOULSTONE_STAIRS = register("soulstone_stairs", id -> new BaseStairsBlock(id, SOULSTONE.defaultBlockState()));
    public static final Block SOULSTONE_COBBLE_STAIRS = register("soulstone_cobble_stairs", id -> new BaseStairsBlock(id, SOULSTONE_COBBLE.defaultBlockState()));
    public static final Block SOULSTONE_BRICKS_STAIRS = register("soulstone_bricks_stairs", id -> new BaseStairsBlock(id, SOULSTONE_BRICKS.defaultBlockState()));
    public static final Block SOULSTONE_COBBLE_WALL = register("soulstone_cobble_wall", id -> new BaseWallBlock(id, () -> SOULSTONE_COBBLE));
    public static final Block SOULSTONE_BRICKS_WALL = register("soulstone_bricks_wall", id -> new BaseWallBlock(id, () -> SOULSTONE_BRICKS));
    public static final Block WITHERPROOF_BLOCK = register("witherproof_block", WitherproofBlock::new);
    public static final Block WITHERPROOF_BRICKS = register("witherproof_bricks", WitherproofBlock::new);
    public static final Block WITHERPROOF_GLASS = register("witherproof_glass", WitherproofGlassBlock::new);
    public static final Block INFUSION_PEDESTAL = register("infusion_pedestal", InfusionPedestalBlock::new);
    public static final Block INFUSION_ALTAR = register("infusion_altar", InfusionAltarBlock::new);
    public static final Block AWAKENING_PEDESTAL = register("awakening_pedestal", AwakeningPedestalBlock::new);
    public static final Block AWAKENING_ALTAR = register("awakening_altar", AwakeningAltarBlock::new);
    public static final Block ESSENCE_VESSEL = register("essence_vessel", EssenceVesselBlock::new);
    public static final Block TINKERING_TABLE = register("tinkering_table", TinkeringTableBlock::new);
    public static final Block ENCHANTER = register("enchanter", EnchanterBlock::new);
    public static final Block MACHINE_FRAME = register("machine_frame", id -> new BaseBlock(id, SoundType.STONE, 1.5F, 6.0F, true));
    public static final Block FURNACE = register("furnace", EssenceFurnaceBlock::new);
    public static final Block REPROCESSOR = register("seed_reprocessor", ReprocessorBlock::new);
    public static final Block SOUL_EXTRACTOR = register("soul_extractor", SoulExtractorBlock::new);
    public static final Block HARVESTER = register("harvester", HarvesterBlock::new);
    public static final Block SOULIUM_SPAWNER = register("soulium_spawner", SouliumSpawnerBlock::new);
    public static final Block ORE_INFUSER = register("ore_infuser", OreInfuserBlock::new);

    public static final Block INFERIUM_CROP = registerNoItem("inferium_crop", id -> new InferiumCropBlock(id, ModCrops.INFERIUM));

    public static void register() {
        ENTRIES.forEach((id, block) -> registerBlock(id, block, MysticalAgriculture.MOD_ID));
        CropRegistry.getInstance().registerBlocks(MysticalCropBlock::new, BLOCK_REGISTRAR);
    }

    public static void registerBlockItems() {
        BLOCK_ITEMS.forEach((id, factory) ->
                ITEM_REGISTRAR.register(id, factory.apply(id), MysticalAgriculture.MOD_ID));
    }

    private static Block register(String name, Function<Identifier, Block> block) {
        return register(name, block, b -> id -> new BaseBlockItem(id, b));
    }

    private static Block register(String name, Function<Identifier, Block> block, Function<Block, Function<Identifier, BlockItem>> item) {
        var value = registerNoItem(name, block);
        var id = MysticalAgriculture.resource(name);
        BLOCK_ITEMS.put(id, item.apply(value));
        return value;
    }

    public static Block registerNoItem(String name, Function<Identifier, Block> block) {
        var id = MysticalAgriculture.resource(name);
        var value = block.apply(id);
        if (ENTRIES.putIfAbsent(id, value) != null) {
            throw new IllegalStateException("Duplicate block id %s contributed by mod %s"
                    .formatted(id, MysticalAgriculture.MOD_ID));
        }
        return value;
    }

    private static void registerBlock(Identifier id, Block block, String sourceMod) {
        BLOCK_REGISTRAR.register(id, block, sourceMod);
    }
}
