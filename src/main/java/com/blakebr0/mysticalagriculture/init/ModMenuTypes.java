package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.container.EnchanterContainer;
import com.blakebr0.mysticalagriculture.container.EssenceFurnaceContainer;
import com.blakebr0.mysticalagriculture.container.HarvesterContainer;
import com.blakebr0.mysticalagriculture.container.OreInfuserContainer;
import com.blakebr0.mysticalagriculture.container.ReprocessorContainer;
import com.blakebr0.mysticalagriculture.container.SoulExtractorContainer;
import com.blakebr0.mysticalagriculture.container.SouliumSpawnerContainer;
import com.blakebr0.mysticalagriculture.container.TinkeringTableContainer;
import com.blakebr0.mysticalagriculture.tileentity.EnchanterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.TinkeringTableTileEntity;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModMenuTypes {
    private static final Map<Identifier, MenuType<?>> ENTRIES = new LinkedHashMap<>();

    public static final MenuType<TinkeringTableContainer> TINKERING_TABLE = register("tinkering_table",
            (id, inventory, pos) -> new TinkeringTableContainer(id, inventory, TinkeringTableTileEntity.createInventoryHandler(), pos));
    public static final MenuType<EnchanterContainer> ENCHANTER = register("enchanter",
            (id, inventory, pos) -> new EnchanterContainer(id, inventory, EnchanterTileEntity.createInventoryHandler(), pos));
    public static final MenuType<EssenceFurnaceContainer> FURNACE = register("furnace",
            (id, inventory, pos) -> new EssenceFurnaceContainer(id, inventory, EssenceFurnaceTileEntity.createInventoryHandler(), new MachineUpgradeItemStackHandler(), new SimpleContainerData(6), pos));
    public static final MenuType<ReprocessorContainer> REPROCESSOR = register("reprocessor",
            (id, inventory, pos) -> new ReprocessorContainer(id, inventory, ReprocessorTileEntity.createInventoryHandler(), new MachineUpgradeItemStackHandler(), new SimpleContainerData(6), pos));
    public static final MenuType<SoulExtractorContainer> SOUL_EXTRACTOR = register("soul_extractor",
            (id, inventory, pos) -> new SoulExtractorContainer(id, inventory, SoulExtractorTileEntity.createInventoryHandler(), new MachineUpgradeItemStackHandler(), new SimpleContainerData(6), pos));
    public static final MenuType<HarvesterContainer> HARVESTER = register("harvester",
            (id, inventory, pos) -> new HarvesterContainer(id, inventory, HarvesterTileEntity.createInventoryHandler(), new MachineUpgradeItemStackHandler(), new SimpleContainerData(4), pos));
    public static final MenuType<SouliumSpawnerContainer> SOULIUM_SPAWNER = register("soulium_spawner",
            (id, inventory, pos) -> new SouliumSpawnerContainer(id, inventory, SouliumSpawnerTileEntity.createInventoryHandler(), new MachineUpgradeItemStackHandler(), new SimpleContainerData(6), pos));
    public static final MenuType<OreInfuserContainer> ORE_INFUSER = register("ore_infuser",
            (id, inventory, pos) -> new OreInfuserContainer(id, inventory, OreInfuserTileEntity.createInventoryHandler(), new MachineUpgradeItemStackHandler(), new SimpleContainerData(6), pos));

    public static void register() {
        ENTRIES.forEach((id, type) -> Registry.register(BuiltInRegistries.MENU, id, type));
    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            ExtendedMenuType.ExtendedFactory<T, BlockPos> factory
    ) {
        var id = MysticalAgriculture.resource(name);
        var type = new ExtendedMenuType<>(factory, BlockPos.STREAM_CODEC);
        ENTRIES.put(id, type);
        return type;
    }
}
