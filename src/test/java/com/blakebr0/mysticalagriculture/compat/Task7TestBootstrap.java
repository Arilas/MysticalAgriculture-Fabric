package com.blakebr0.mysticalagriculture.compat;

import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModRecipeSerializers;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;

final class Task7TestBootstrap {
    private static boolean initialized;

    private Task7TestBootstrap() {
    }

    static synchronized void ensureInitialized() {
        if (initialized)
            return;

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ModBlocks.register();
        ModBlocks.registerBlockItems();
        ModItems.register();
        ModDataComponentTypes.register();
        ModRecipeSerializers.register();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
                .forEach(pending -> pending.apply());
        ((MappedRegistry<Item>) BuiltInRegistries.ITEM).bindAllTagsToEmpty();
        BuiltInRegistries.ITEM.freeze();
        initialized = true;
    }
}
