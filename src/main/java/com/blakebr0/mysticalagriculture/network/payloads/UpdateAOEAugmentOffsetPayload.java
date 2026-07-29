package com.blakebr0.mysticalagriculture.network.payloads;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateAOEAugmentOffsetPayload(int horizontalOffsetChange, int verticalOffsetChange) implements CustomPacketPayload {
    public static final Type<UpdateAOEAugmentOffsetPayload> TYPE = new Type<>(MysticalAgricultureAPI.resource("update_aoe_offset"));

    public static final StreamCodec<FriendlyByteBuf, UpdateAOEAugmentOffsetPayload> STREAM_CODEC = StreamCodec.of(
            UpdateAOEAugmentOffsetPayload::toNetwork, UpdateAOEAugmentOffsetPayload::fromNetwork
    );

    @Override
    public Type<UpdateAOEAugmentOffsetPayload> type() {
        return TYPE;
    }

    private static UpdateAOEAugmentOffsetPayload fromNetwork(ByteBuf buf) {
        var horizontal = buf.readInt();
        var vertical = buf.readInt();

        if (!isSingleStep(horizontal) || !isSingleStep(vertical)) {
            throw new IllegalArgumentException("AOE offset changes must be single-step deltas");
        }

        return new UpdateAOEAugmentOffsetPayload(horizontal, vertical);
    }

    private static void toNetwork(ByteBuf buf, UpdateAOEAugmentOffsetPayload payload) {
        buf.writeInt(payload.horizontalOffsetChange);
        buf.writeInt(payload.verticalOffsetChange);
    }

    public boolean hasOnlySingleStepChanges() {
        return isSingleStep(this.horizontalOffsetChange) && isSingleStep(this.verticalOffsetChange);
    }

    private static boolean isSingleStep(int change) {
        return change >= -1 && change <= 1;
    }
}
