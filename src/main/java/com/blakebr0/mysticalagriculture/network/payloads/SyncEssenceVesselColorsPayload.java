package com.blakebr0.mysticalagriculture.network.payloads;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;

public record SyncEssenceVesselColorsPayload(Map<String, Integer> colors) implements CustomPacketPayload {
    public static final Type<SyncEssenceVesselColorsPayload> TYPE = new Type<>(MysticalAgricultureAPI.resource("sync_essence_vessel_colors"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEssenceVesselColorsPayload> STREAM_CODEC = StreamCodec.of(
            SyncEssenceVesselColorsPayload::toNetwork, SyncEssenceVesselColorsPayload::fromNetwork
    );

    @Override
    public Type<SyncEssenceVesselColorsPayload> type() {
        return TYPE;
    }

    private static SyncEssenceVesselColorsPayload fromNetwork(RegistryFriendlyByteBuf buffer) {
        var colors = buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readVarInt);
        return new SyncEssenceVesselColorsPayload(colors);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, SyncEssenceVesselColorsPayload payload) {
        buffer.writeMap(payload.colors, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeVarInt);
    }
}
