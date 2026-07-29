package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class ModStorageProviders {
    private ModStorageProviders() {
    }

    public static void register() {
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> storage(block.getInventory(), side),
                ModTileEntities.AWAKENING_ALTAR
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> storage(block.getInventory(), side),
                ModTileEntities.AWAKENING_PEDESTAL
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> storage(block.getInventory(), side),
                ModTileEntities.ESSENCE_VESSEL
        );
        ItemStorage.SIDED.registerForBlockEntity(EssenceFurnaceTileEntity::getSidedInventory, ModTileEntities.FURNACE);
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> storage(block.getInventory(), side),
                ModTileEntities.HARVESTER
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> storage(block.getInventory(), side),
                ModTileEntities.INFUSION_ALTAR
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> storage(block.getInventory(), side),
                ModTileEntities.INFUSION_PEDESTAL
        );
        ItemStorage.SIDED.registerForBlockEntity(ReprocessorTileEntity::getSidedInventory, ModTileEntities.REPROCESSOR);
        ItemStorage.SIDED.registerForBlockEntity(SoulExtractorTileEntity::getSidedInventory, ModTileEntities.SOUL_EXTRACTOR);
        ItemStorage.SIDED.registerForBlockEntity(SouliumSpawnerTileEntity::getSidedInventory, ModTileEntities.SOULIUM_SPAWNER);
        ItemStorage.SIDED.registerForBlockEntity(OreInfuserTileEntity::getSidedInventory, ModTileEntities.ORE_INFUSER);

        EnergyStorage.SIDED.registerForBlockEntity((block, side) -> energy(block.getEnergy(), side), ModTileEntities.FURNACE);
        EnergyStorage.SIDED.registerForBlockEntity((block, side) -> energy(block.getEnergy(), side), ModTileEntities.HARVESTER);
        EnergyStorage.SIDED.registerForBlockEntity((block, side) -> energy(block.getEnergy(), side), ModTileEntities.REPROCESSOR);
        EnergyStorage.SIDED.registerForBlockEntity((block, side) -> energy(block.getEnergy(), side), ModTileEntities.SOUL_EXTRACTOR);
        EnergyStorage.SIDED.registerForBlockEntity((block, side) -> energy(block.getEnergy(), side), ModTileEntities.SOULIUM_SPAWNER);
        EnergyStorage.SIDED.registerForBlockEntity((block, side) -> energy(block.getEnergy(), side), ModTileEntities.ORE_INFUSER);
    }

    private static @Nullable Storage<ItemVariant> storage(
            CItemStacksHandler inventory,
            @Nullable Direction side
    ) {
        return side == null ? null : MachineItemStorage.of(inventory);
    }

    private static @Nullable EnergyStorage energy(
            EnergyStorage energy,
            @Nullable Direction side
    ) {
        return side == null ? null : energy;
    }
}
