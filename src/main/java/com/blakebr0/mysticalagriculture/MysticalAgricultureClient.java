package com.blakebr0.mysticalagriculture;

import com.blakebr0.cucumber.event.RegisterClientItemsEvent;
import com.blakebr0.mysticalagriculture.client.ModClientTooltipComponentFactories;
import com.blakebr0.mysticalagriculture.client.ModMenuScreens;
import com.blakebr0.mysticalagriculture.client.ModTESRs;
import com.blakebr0.mysticalagriculture.client.handler.AOEAugmentClientHandler;
import com.blakebr0.mysticalagriculture.client.handler.ClientRecipeHandler;
import com.blakebr0.mysticalagriculture.client.handler.ClientNetworkHandler;
import com.blakebr0.mysticalagriculture.client.handler.GuiOverlayHandler;
import com.blakebr0.mysticalagriculture.client.handler.ItemModelPropertyHandler;
import com.blakebr0.mysticalagriculture.client.handler.MachineBlockTooltipHandler;
import com.blakebr0.mysticalagriculture.client.handler.MachineUpgradeTooltipHandler;
import com.blakebr0.mysticalagriculture.client.handler.ModelHandler;
import com.blakebr0.mysticalagriculture.client.handler.TintSourceHandler;
import net.fabricmc.api.ClientModInitializer;

public final class MysticalAgricultureClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemModelPropertyHandler.register();
        TintSourceHandler.register();
        ModelHandler.register();
        RegisterClientItemsEvent.EVENT.register(ModelHandler::onRegisterClientItems);

        ModMenuScreens.register();
        ModTESRs.register();
        ModClientTooltipComponentFactories.register();

        ClientNetworkHandler.register();
        ClientRecipeHandler.register();
        GuiOverlayHandler.register();
        AOEAugmentClientHandler.register();
        MachineBlockTooltipHandler.register();
        MachineUpgradeTooltipHandler.register();
    }
}
