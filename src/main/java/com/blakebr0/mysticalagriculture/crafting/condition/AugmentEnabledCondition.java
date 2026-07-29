package com.blakebr0.mysticalagriculture.crafting.condition;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.registry.AugmentRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

public record AugmentEnabledCondition(Identifier augment) implements ResourceCondition {
    public static final MapCodec<AugmentEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("augment").forGetter(AugmentEnabledCondition::augment)
            ).apply(builder, AugmentEnabledCondition::new)
    );
    public static final ResourceConditionType<AugmentEnabledCondition> TYPE =
            ResourceConditionType.create(
                    MysticalAgricultureAPI.resource("augment_enabled"),
                    CODEC
            );

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        var value = AugmentRegistry.getInstance().getAugmentById(this.augment);
        return value != null && value.isEnabled();
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }
}
