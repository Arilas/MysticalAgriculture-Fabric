package com.blakebr0.mysticalagriculture.client;

import com.blakebr0.mysticalagriculture.client.screen.EnchanterScreen;
import com.blakebr0.mysticalagriculture.client.screen.EssenceFurnaceScreen;
import com.blakebr0.mysticalagriculture.client.screen.HarvesterScreen;
import com.blakebr0.mysticalagriculture.client.screen.OreInfuserScreen;
import com.blakebr0.mysticalagriculture.client.screen.ReprocessorScreen;
import com.blakebr0.mysticalagriculture.client.screen.SoulExtractorScreen;
import com.blakebr0.mysticalagriculture.client.screen.SouliumSpawnerScreen;
import com.blakebr0.mysticalagriculture.client.screen.TinkeringTableScreen;
import com.blakebr0.mysticalagriculture.init.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;

public final class ModMenuScreens {
    private ModMenuScreens() {
    }

    public static void register() {
        MenuScreens.register(ModMenuTypes.TINKERING_TABLE, TinkeringTableScreen::new);
        MenuScreens.register(ModMenuTypes.ENCHANTER, EnchanterScreen::new);
        MenuScreens.register(ModMenuTypes.FURNACE, EssenceFurnaceScreen::new);
        MenuScreens.register(ModMenuTypes.REPROCESSOR, ReprocessorScreen::new);
        MenuScreens.register(ModMenuTypes.SOUL_EXTRACTOR, SoulExtractorScreen::new);
        MenuScreens.register(ModMenuTypes.HARVESTER, HarvesterScreen::new);
        MenuScreens.register(ModMenuTypes.SOULIUM_SPAWNER, SouliumSpawnerScreen::new);
        MenuScreens.register(ModMenuTypes.ORE_INFUSER, OreInfuserScreen::new);
    }
}
