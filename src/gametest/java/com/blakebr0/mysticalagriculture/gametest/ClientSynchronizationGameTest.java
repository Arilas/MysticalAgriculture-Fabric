package com.blakebr0.mysticalagriculture.gametest;

import com.blakebr0.mysticalagriculture.client.handler.ClientRecipeHandler;
import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
import com.blakebr0.mysticalagriculture.network.payloads.ReloadIngredientCachePayload;
import com.blakebr0.mysticalagriculture.network.payloads.SyncEssenceVesselColorsPayload;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClientSynchronizationGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        clearClientState();
        require(snapshot().isEmpty(), "client state was not empty before joining a world");

        try (var cachePayloads = new PayloadProbe<>(ReloadIngredientCachePayload.TYPE);
             var colorPayloads = new PayloadProbe<>(SyncEssenceVesselColorsPayload.TYPE)) {
            TestWorldSave save;
            StateSnapshot joined;
            try (var world = context.worldBuilder().create()) {
                save = world.getWorldSave();
                context.waitFor(_ -> snapshot().isPopulated()
                        && cachePayloads.receptions() == 1
                        && colorPayloads.receptions() == 1);
                joined = snapshot();

                clearClientState();
                require(snapshot().isEmpty(), "client state did not clear before reload");
                world.getServer().runCommand("reload");
                context.waitFor(_ -> snapshot().isPopulated()
                        && cachePayloads.receptions() == 2
                        && colorPayloads.receptions() == 2);
                require(joined.equals(snapshot()), "reload produced incoherent client synchronization state");
            }

            context.waitFor(_ -> snapshot().isEmpty());

            try (var reopened = save.open()) {
                context.waitFor(_ -> snapshot().isPopulated()
                        && cachePayloads.receptions() == 3
                        && colorPayloads.receptions() == 3);
                require(joined.equals(snapshot()), "reconnecting to the same save produced different client state");
            }

            context.waitFor(_ -> snapshot().isEmpty());
        }
    }

    private static StateSnapshot snapshot() {
        return new StateSnapshot(
                RecipeIngredientCache.INSTANCE.copyCaches().size(),
                RecipeIngredientCache.INSTANCE.copyValidVesselItems().size(),
                EssenceVesselColorManager.INSTANCE.copyColors().size(),
                ClientRecipeHandler.AWAKENING_RECIPES.size()
                        + ClientRecipeHandler.ENCHANTER_RECIPES.size()
                        + ClientRecipeHandler.INFUSION_RECIPES.size()
                        + ClientRecipeHandler.REPROCESSOR_RECIPES.size()
                        + ClientRecipeHandler.SOUL_EXTRACTION_RECIPES.size()
                        + ClientRecipeHandler.SOULIUM_SPAWNER_RECIPES.size()
                        + ClientRecipeHandler.ORE_INFUSION_RECIPES.size()
        );
    }

    private static void clearClientState() {
        RecipeIngredientCache.INSTANCE.setState(Map.of(), Set.of());
        EssenceVesselColorManager.INSTANCE.setColors(Map.of());
        ClientRecipeHandler.clear();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record StateSnapshot(int cacheTypes, int vesselItems, int colors, int synchronizedRecipes) {
        private boolean isEmpty() {
            return this.cacheTypes == 0
                    && this.vesselItems == 0
                    && this.colors == 0
                    && this.synchronizedRecipes == 0;
        }

        private boolean isPopulated() {
            return this.cacheTypes > 0
                    && this.vesselItems > 0
                    && this.colors > 0
                    && this.synchronizedRecipes > 0;
        }
    }

    private static final class PayloadProbe<T extends CustomPacketPayload> implements AutoCloseable {
        private final CustomPacketPayload.Type<T> type;
        private final ClientPlayNetworking.PlayPayloadHandler<T> original;
        private final AtomicInteger receptions = new AtomicInteger();

        @SuppressWarnings("unchecked")
        private PayloadProbe(CustomPacketPayload.Type<T> type) {
            this.type = type;
            this.original = (ClientPlayNetworking.PlayPayloadHandler<T>)
                    ClientPlayNetworking.unregisterGlobalReceiver(type.id());
            require(this.original != null, "production payload receiver was not registered for " + type.id());
            require(ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                this.receptions.incrementAndGet();
                this.original.receive(payload, context);
            }), "could not install payload probe for " + type.id());
        }

        private int receptions() {
            return this.receptions.get();
        }

        @Override
        public void close() {
            ClientPlayNetworking.unregisterGlobalReceiver(this.type.id());
            require(ClientPlayNetworking.registerGlobalReceiver(this.type, this.original),
                    "could not restore production payload receiver for " + this.type.id());
        }
    }
}
