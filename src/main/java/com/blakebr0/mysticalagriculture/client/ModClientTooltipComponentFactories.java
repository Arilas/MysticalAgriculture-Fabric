package com.blakebr0.mysticalagriculture.client;

import com.blakebr0.mysticalagriculture.client.handler.AugmentTooltipHandler;
import com.blakebr0.mysticalagriculture.item.AugmentItem;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;

public final class ModClientTooltipComponentFactories {
    private static boolean registered;

    private ModClientTooltipComponentFactories() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        ClientTooltipComponentCallback.EVENT.register(data -> data instanceof AugmentItem.AugmentTooltipData augment
                ? AugmentTooltipHandler.createComponent(augment.types())
                : null);
        registered = true;
    }
}
