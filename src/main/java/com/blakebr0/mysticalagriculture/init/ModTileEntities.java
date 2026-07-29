package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
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
import com.blakebr0.mysticalagriculture.registry.RegistryTypeFactories;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModTileEntities {
    private static final Map<Identifier, BlockEntityType<?>> ENTRIES = new LinkedHashMap<>();

    public static final BlockEntityType<InfusionPedestalTileEntity> INFUSION_PEDESTAL = register("infusion_pedestal", InfusionPedestalTileEntity::new, ModBlocks.INFUSION_PEDESTAL);
    public static final BlockEntityType<InfusionAltarTileEntity> INFUSION_ALTAR = register("infusion_altar", InfusionAltarTileEntity::new, ModBlocks.INFUSION_ALTAR);
    public static final BlockEntityType<AwakeningPedestalTileEntity> AWAKENING_PEDESTAL = register("awakening_pedestal", AwakeningPedestalTileEntity::new, ModBlocks.AWAKENING_PEDESTAL);
    public static final BlockEntityType<AwakeningAltarTileEntity> AWAKENING_ALTAR = register("awakening_altar", AwakeningAltarTileEntity::new, ModBlocks.AWAKENING_ALTAR);
    public static final BlockEntityType<EssenceVesselTileEntity> ESSENCE_VESSEL = register("essence_vessel", EssenceVesselTileEntity::new, ModBlocks.ESSENCE_VESSEL);
    public static final BlockEntityType<TinkeringTableTileEntity> TINKERING_TABLE = register("tinkering_table", TinkeringTableTileEntity::new, ModBlocks.TINKERING_TABLE);
    public static final BlockEntityType<EnchanterTileEntity> ENCHANTER = register("enchanter", EnchanterTileEntity::new, ModBlocks.ENCHANTER);
    public static final BlockEntityType<EssenceFurnaceTileEntity> FURNACE = register("furnace", EssenceFurnaceTileEntity::new, ModBlocks.FURNACE);
    public static final BlockEntityType<ReprocessorTileEntity> REPROCESSOR = register("seed_reprocessor", ReprocessorTileEntity::new, ModBlocks.REPROCESSOR);
    public static final BlockEntityType<SoulExtractorTileEntity> SOUL_EXTRACTOR = register("soul_extractor", SoulExtractorTileEntity::new, ModBlocks.SOUL_EXTRACTOR);
    public static final BlockEntityType<HarvesterTileEntity> HARVESTER = register("harvester", HarvesterTileEntity::new, ModBlocks.HARVESTER);
    public static final BlockEntityType<SouliumSpawnerTileEntity> SOULIUM_SPAWNER = register("soulium_spawner", SouliumSpawnerTileEntity::new, ModBlocks.SOULIUM_SPAWNER);
    public static final BlockEntityType<OreInfuserTileEntity> ORE_INFUSER = register("ore_infuser", OreInfuserTileEntity::new, ModBlocks.ORE_INFUSER);

    public static void register() {
        ENTRIES.forEach((id, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type));
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Block... blocks
    ) {
        var id = MysticalAgriculture.resource(name);
        var type = RegistryTypeFactories.blockEntity(factory, blocks);
        ENTRIES.put(id, type);
        return type;
    }
}
