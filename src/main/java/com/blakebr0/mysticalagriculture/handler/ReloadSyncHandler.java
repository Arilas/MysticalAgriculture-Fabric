package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
import com.blakebr0.mysticalagriculture.network.payloads.ReloadIngredientCachePayload;
import com.blakebr0.mysticalagriculture.network.payloads.SyncEssenceVesselColorsPayload;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ReloadSyncHandler {
    private ReloadSyncHandler() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((listener, _, _) -> sync(listener.player));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, _, success) -> {
            if (success) {
                sync(server);
            }
        });
    }

    public static void sync(MinecraftServer server) {
        for (var player : server.getPlayerList().getPlayers()) {
            sync(player);
        }
    }

    public static void sync(ServerPlayer player) {
        var cache = new ReloadIngredientCachePayload(
                RecipeIngredientCache.INSTANCE.copyCaches(),
                RecipeIngredientCache.INSTANCE.copyValidVesselItems()
        );
        var colors = new SyncEssenceVesselColorsPayload(
                EssenceVesselColorManager.INSTANCE.copyColors()
        );

        if (ServerPlayNetworking.canSend(player, ReloadIngredientCachePayload.TYPE)) {
            ServerPlayNetworking.send(player, cache);
        }
        if (ServerPlayNetworking.canSend(player, SyncEssenceVesselColorsPayload.TYPE)) {
            ServerPlayNetworking.send(player, colors);
        }
    }
}
