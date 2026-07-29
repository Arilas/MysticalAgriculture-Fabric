package com.blakebr0.mysticalagriculture.api;

import com.blakebr0.mysticalagriculture.api.components.AOEAugmentOffsetComponent;
import com.blakebr0.mysticalagriculture.api.components.AugmentComponent;
import com.blakebr0.mysticalagriculture.api.components.SoulJarComponent;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public final class MysticalAgricultureDataComponentTypes {
    public static final DataComponentType<List<AugmentComponent>> EQUIPPED_AUGMENTS =
            DataComponentType.<List<AugmentComponent>>builder()
                    .persistent(AugmentComponent.EQUIPPED_CODEC)
                    .networkSynchronized(AugmentComponent.EQUIPPED_STREAM_CODEC)
                    .build();
    public static final DataComponentType<Integer> EXPERIENCE_CAPSULE =
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build();
    public static final DataComponentType<SoulJarComponent> SOUL_JAR =
            DataComponentType.<SoulJarComponent>builder()
                    .persistent(SoulJarComponent.CODEC)
                    .networkSynchronized(SoulJarComponent.STREAM_CODEC)
                    .build();
    public static final DataComponentType<AOEAugmentOffsetComponent> AOE_AUGMENT_OFFSET =
            DataComponentType.<AOEAugmentOffsetComponent>builder()
                    .persistent(AOEAugmentOffsetComponent.CODEC)
                    .networkSynchronized(AOEAugmentOffsetComponent.STREAM_CODEC)
                    .build();
}
