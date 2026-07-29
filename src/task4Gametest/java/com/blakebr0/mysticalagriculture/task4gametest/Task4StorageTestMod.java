package com.blakebr0.mysticalagriculture.task4gametest;

import com.blakebr0.mysticalagriculture.handler.MachineItemStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class Task4StorageTestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(
                        "mysticalagriculture-task4-gametest",
                        "test_machine"
                ),
                TestMachineBlockEntity.TYPE
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (block, side) -> MachineItemStorage.sided(block.sidedInventoryWrappers(), side),
                TestMachineBlockEntity.TYPE
        );
    }
}
