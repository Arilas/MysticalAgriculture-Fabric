package com.blakebr0.mysticalagriculture.network;

import com.blakebr0.mysticalagriculture.api.components.AOEAugmentOffsetComponent;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import com.blakebr0.mysticalagriculture.network.payloads.ExperienceCapsulePickupPayload;
import com.blakebr0.mysticalagriculture.network.payloads.ReloadIngredientCachePayload;
import com.blakebr0.mysticalagriculture.network.payloads.SyncEssenceVesselColorsPayload;
import com.blakebr0.mysticalagriculture.network.payloads.UpdateAOEAugmentOffsetPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class NetworkHandler {
    private NetworkHandler() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(
                ExperienceCapsulePickupPayload.TYPE,
                ExperienceCapsulePickupPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
                ReloadIngredientCachePayload.TYPE,
                ReloadIngredientCachePayload.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
                SyncEssenceVesselColorsPayload.TYPE,
                SyncEssenceVesselColorsPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                UpdateAOEAugmentOffsetPayload.TYPE,
                UpdateAOEAugmentOffsetPayload.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateAOEAugmentOffsetPayload.TYPE,
                (payload, context) -> applyAOEOffset(context.player(), payload)
        );
    }

    static boolean applyAOEOffset(ServerPlayer player, UpdateAOEAugmentOffsetPayload payload) {
        if (!payload.hasOnlySingleStepChanges()) {
            return false;
        }

        var stack = player.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ITinkerable)) {
            return false;
        }

        var range = AugmentUtils.getMaxAOEAugmentRange(stack);
        if (range <= 0) {
            return false;
        }

        var offset = stack.getOrDefault(
                ModDataComponentTypes.AOE_AUGMENT_OFFSET,
                AOEAugmentOffsetComponent.DEFAULT
        );
        var horizontalOffset = Mth.clamp(offset.horizontalOffset(), -range, range);
        var verticalOffset = Mth.clamp(offset.verticalOffset(), -range, range);
        stack.set(ModDataComponentTypes.AOE_AUGMENT_OFFSET, new AOEAugmentOffsetComponent(
                Mth.clamp(horizontalOffset + payload.horizontalOffsetChange(), -range, range),
                Mth.clamp(verticalOffset + payload.verticalOffsetChange(), -range, range)
        ));
        return true;
    }
}
