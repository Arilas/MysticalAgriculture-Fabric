package com.blakebr0.mysticalagriculture.compat;

import com.blakebr0.mysticalagriculture.compat.jei.JeiCompat;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OptionalIntegrationApiTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void pluginsImplementThePublishedFabricDiscoveryApis() {
        assertInstanceOf(IModPlugin.class, new JeiCompat());
        assertInstanceOf(IWailaPlugin.class, new JadeCompat());
        assertNotNull(JadeCompat.class.getAnnotation(WailaPlugin.class));
    }

    @Test
    void jeiRegistersTransfersForEveryMenuBackedRecipeCategory() {
        var transfers = new ArrayList<Transfer>();
        var registration = proxy(IRecipeTransferRegistration.class, (method, args) -> {
            if (method.getName().equals("addRecipeTransferHandler") && args.length == 7) {
                transfers.add(new Transfer(
                        ((Class<?>) args[0]).getSimpleName(),
                        ((IRecipeType<?>) args[2]).getUid().toString(),
                        (int) args[3],
                        (int) args[4],
                        (int) args[5],
                        (int) args[6]
                ));
            }
        });

        new JeiCompat().registerRecipeTransferHandlers(registration);

        assertEquals(List.of(
                new Transfer("EnchanterContainer", "mysticalagriculture:enchanter", 0, 3, 4, 36),
                new Transfer("EssenceFurnaceContainer", "minecraft:smelting", 1, 1, 4, 36),
                new Transfer("ReprocessorContainer", "mysticalagriculture:reprocessor", 1, 1, 4, 36),
                new Transfer("SoulExtractorContainer", "mysticalagriculture:soul_extractor", 1, 1, 4, 36),
                new Transfer("SouliumSpawnerContainer", "mysticalagriculture:soulium_spawner", 1, 1, 3, 36),
                new Transfer("OreInfuserContainer", "mysticalagriculture:ore_infuser", 1, 2, 5, 36)
        ), transfers);
    }

    @Test
    void jadeRegistersCropComponentsAndNativeMachineProgress() {
        var clientTargets = new ArrayList<String>();
        var progressTargets = new ArrayList<String>();
        var client = proxy(IWailaClientRegistration.class, (method, args) -> {
            if (method.getName().equals("registerBlockComponent")) {
                clientTargets.add(((Class<?>) args[1]).getSimpleName());
            }
        });
        var common = proxy(IWailaCommonRegistration.class, (method, args) -> {
            if (method.getName().equals("registerProgress")) {
                progressTargets.add(((Class<?>) args[1]).getSimpleName());
            }
        });
        var plugin = new JadeCompat();

        plugin.register(common);
        plugin.registerClient(client);

        assertEquals(List.of(
                "MysticalCropBlock",
                "InferiumCropBlock",
                "InfusedFarmlandBlock"
        ), clientTargets);
        assertEquals(List.of(
                "EssenceFurnaceTileEntity",
                "HarvesterTileEntity",
                "ReprocessorTileEntity",
                "SoulExtractorTileEntity",
                "SouliumSpawnerTileEntity",
                "OreInfuserTileEntity"
        ), progressTargets);
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (_, method, args) -> {
            var actualArgs = args == null ? new Object[0] : args;
            invocation.accept(method, actualArgs);
            return defaultValue(method.getReturnType());
        }));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class)
            return null;
        if (!type.isPrimitive())
            return null;
        if (type == boolean.class)
            return false;
        if (type == char.class)
            return '\0';
        if (type == byte.class)
            return (byte) 0;
        if (type == short.class)
            return (short) 0;
        if (type == int.class)
            return 0;
        if (type == long.class)
            return 0L;
        if (type == float.class)
            return 0F;
        if (type == double.class)
            return 0D;
        throw new IllegalArgumentException("Unsupported primitive " + type);
    }

    @FunctionalInterface
    private interface Invocation {
        void accept(java.lang.reflect.Method method, Object[] args);
    }

    private record Transfer(
            String container,
            String recipeType,
            int recipeSlotStart,
            int recipeSlotCount,
            int inventorySlotStart,
            int inventorySlotCount
    ) {
    }
}
