package com.blakebr0.mysticalagriculture.network.payloads;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ExperienceCapsulePickupPayload() implements CustomPacketPayload {
    public static final Type<ExperienceCapsulePickupPayload> TYPE = new Type<>(MysticalAgricultureAPI.resource("experience_pickup"));

    public static final StreamCodec<FriendlyByteBuf, ExperienceCapsulePickupPayload> STREAM_CODEC = StreamCodec.unit(new ExperienceCapsulePickupPayload());

    @Override
    public Type<ExperienceCapsulePickupPayload> type() {
        return TYPE;
    }
}
