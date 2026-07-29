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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ModMenuScreens {
    @SubscribeEvent
    public void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TINKERING_TABLE, TinkeringTableScreen::new);
        event.register(ModMenuTypes.ENCHANTER, EnchanterScreen::new);
        event.register(ModMenuTypes.FURNACE, EssenceFurnaceScreen::new);
        event.register(ModMenuTypes.REPROCESSOR, ReprocessorScreen::new);
        event.register(ModMenuTypes.SOUL_EXTRACTOR, SoulExtractorScreen::new);
        event.register(ModMenuTypes.HARVESTER, HarvesterScreen::new);
        event.register(ModMenuTypes.SOULIUM_SPAWNER, SouliumSpawnerScreen::new);
        event.register(ModMenuTypes.ORE_INFUSER, OreInfuserScreen::new);
    }
}
