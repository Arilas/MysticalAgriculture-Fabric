package com.blakebr0.mysticalagriculture.api;

import com.blakebr0.mysticalagriculture.api.components.AOEAugmentOffsetComponent;
import com.blakebr0.mysticalagriculture.api.components.AugmentComponent;
import com.blakebr0.mysticalagriculture.api.components.SoulJarComponent;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public final class MysticalAgricultureDataComponentTypes {
    public static final DataComponentType<List<AugmentComponent>> EQUIPPED_AUGMENTS = ModDataComponentTypes.EQUIPPED_AUGMENTS;
    public static final DataComponentType<Integer> EXPERIENCE_CAPSULE = ModDataComponentTypes.EXPERIENCE_CAPSULE;
    public static final DataComponentType<SoulJarComponent> SOUL_JAR = ModDataComponentTypes.SOUL_JAR;
    public static final DataComponentType<AOEAugmentOffsetComponent> AOE_AUGMENT_OFFSET = ModDataComponentTypes.AOE_AUGMENT_OFFSET;
}
