package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
import com.blakebr0.mysticalagriculture.network.payloads.ExperienceCapsulePickupPayload;
import com.blakebr0.mysticalagriculture.network.payloads.ReloadIngredientCachePayload;
import com.blakebr0.mysticalagriculture.network.payloads.SyncEssenceVesselColorsPayload;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.sounds.SoundEvents;

import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public final class ClientNetworkHandler {
    private static boolean registered;

    private ClientNetworkHandler() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        ClientPlayNetworking.registerGlobalReceiver(ExperienceCapsulePickupPayload.TYPE, (_, context) -> {
            context.player().playSound(
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    0.1F,
                    (Utils.RANDOM.nextFloat() - Utils.RANDOM.nextFloat()) * 0.35F + 0.9F
            );
        });
        ClientPlayNetworking.registerGlobalReceiver(ReloadIngredientCachePayload.TYPE, (payload, _) -> {
            RecipeIngredientCache.INSTANCE.setState(payload.caches(), payload.validVesselItems());
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncEssenceVesselColorsPayload.TYPE, (payload, _) -> {
            EssenceVesselColorManager.INSTANCE.setColors(payload.colors());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> clearServerState());
        registered = true;
    }

    static void clearServerState() {
        RecipeIngredientCache.INSTANCE.setState(Map.of(), Set.of());
        EssenceVesselColorManager.INSTANCE.setColors(Map.of());
    }
}
