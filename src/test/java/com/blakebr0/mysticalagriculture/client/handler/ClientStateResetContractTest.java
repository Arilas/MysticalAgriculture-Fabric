package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientStateResetContractTest {
    @BeforeAll
    public static void bootstrapVanillaRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void disconnectResetClearsRecipesIngredientCachesAndVesselColors() {
        ClientRecipeHandler.AWAKENING_RECIPES.add(null);
        ClientRecipeHandler.ENCHANTER_RECIPES.add(null);
        ClientRecipeHandler.INFUSION_RECIPES.add(null);
        ClientRecipeHandler.REPROCESSOR_RECIPES.add(null);
        ClientRecipeHandler.SOUL_EXTRACTION_RECIPES.add(null);
        ClientRecipeHandler.SOULIUM_SPAWNER_RECIPES.add(null);
        ClientRecipeHandler.ORE_INFUSION_RECIPES.add(null);

        var id = Identifier.fromNamespaceAndPath("mysticalagriculture", "test");
        ClientRecipeHandler.AWAKENING_RECIPE_MAP.put(id, null);
        ClientRecipeHandler.INFUSION_RECIPE_MAP.put(id, null);
        ClientRecipeHandler.SOULIUM_SPAWNER_RECIPE_MAP.put(id, null);

        RecipeIngredientCache.INSTANCE.setState(Map.of(), Set.of(Items.STONE));
        EssenceVesselColorManager.INSTANCE.setColors(Map.of("minecraft:stone", 0x123456));

        ClientRecipeHandler.clear();
        ClientNetworkHandler.clearServerState();

        assertTrue(ClientRecipeHandler.AWAKENING_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.ENCHANTER_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.INFUSION_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.REPROCESSOR_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.SOUL_EXTRACTION_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.SOULIUM_SPAWNER_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.ORE_INFUSION_RECIPES.isEmpty());
        assertTrue(ClientRecipeHandler.AWAKENING_RECIPE_MAP.isEmpty());
        assertTrue(ClientRecipeHandler.INFUSION_RECIPE_MAP.isEmpty());
        assertTrue(ClientRecipeHandler.SOULIUM_SPAWNER_RECIPE_MAP.isEmpty());
        assertTrue(RecipeIngredientCache.INSTANCE.copyValidVesselItems().isEmpty());
        assertTrue(EssenceVesselColorManager.INSTANCE.copyColors().isEmpty());
    }
}
