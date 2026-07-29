package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class RegisterCapabilityHandler {
    @SubscribeEvent
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.AWAKENING_ALTAR, (block, _) -> block.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.AWAKENING_PEDESTAL, (block, _) -> block.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.ESSENCE_VESSEL, (block, _) -> block.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.FURNACE, EssenceFurnaceTileEntity::getSidedInventory);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.HARVESTER, (block, _) -> block.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.INFUSION_ALTAR, (block, _) -> block.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.INFUSION_PEDESTAL, (block, _) -> block.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.REPROCESSOR, ReprocessorTileEntity::getSidedInventory);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.SOUL_EXTRACTOR, SoulExtractorTileEntity::getSidedInventory);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.SOULIUM_SPAWNER, SouliumSpawnerTileEntity::getSidedInventory);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModTileEntities.ORE_INFUSER, OreInfuserTileEntity::getSidedInventory);

        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.FURNACE, (block, _) -> block.getEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.HARVESTER, (block, _) -> block.getEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.REPROCESSOR, (block, _) -> block.getEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.SOUL_EXTRACTOR, (block, _) -> block.getEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.SOULIUM_SPAWNER, (block, _) -> block.getEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModTileEntities.ORE_INFUSER, (block, _) -> block.getEnergy());
    }
}
