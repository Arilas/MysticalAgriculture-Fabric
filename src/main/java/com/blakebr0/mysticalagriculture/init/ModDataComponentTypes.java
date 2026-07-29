package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureDataComponentTypes;
import com.blakebr0.mysticalagriculture.api.components.AOEAugmentOffsetComponent;
import com.blakebr0.mysticalagriculture.api.components.AugmentComponent;
import com.blakebr0.mysticalagriculture.api.components.SoulJarComponent;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModDataComponentTypes {
    private static final Map<Identifier, DataComponentType<?>> ENTRIES = new LinkedHashMap<>();

    public static final DataComponentType<Boolean> WATERING_CAN_ACTIVE = register("watering_can_active",
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DataComponentType<List<AugmentComponent>> EQUIPPED_AUGMENTS = register("equipped_augments",
            MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS);
    public static final DataComponentType<Integer> EXPERIENCE_CAPSULE = register("experience_capsule",
            MysticalAgricultureDataComponentTypes.EXPERIENCE_CAPSULE);
    public static final DataComponentType<SoulJarComponent> SOUL_JAR = register("soul_jar",
            MysticalAgricultureDataComponentTypes.SOUL_JAR);
    public static final DataComponentType<AOEAugmentOffsetComponent> AOE_AUGMENT_OFFSET = register("aoe_augment_offset",
            MysticalAgricultureDataComponentTypes.AOE_AUGMENT_OFFSET);

    public static void register() {
        ENTRIES.forEach((id, type) -> {
            if (BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(id)) {
                throw new IllegalStateException("Duplicate data component id %s contributed by mod %s"
                        .formatted(id, MysticalAgriculture.MOD_ID));
            }
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type);
        });
    }

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        ENTRIES.put(MysticalAgriculture.resource(name), type);
        return type;
    }
}
