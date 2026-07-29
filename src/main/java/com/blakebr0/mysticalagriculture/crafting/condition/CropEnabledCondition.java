package com.blakebr0.mysticalagriculture.crafting.condition;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

public record CropEnabledCondition(Identifier crop) implements ResourceCondition {
    public static final MapCodec<CropEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("crop").forGetter(CropEnabledCondition::crop)
            ).apply(builder, CropEnabledCondition::new)
    );
    public static final ResourceConditionType<CropEnabledCondition> TYPE =
            ResourceConditionType.create(
                    MysticalAgricultureAPI.resource("crop_enabled"),
                    CODEC
            );

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        var value = CropRegistry.getInstance().getCropById(this.crop);
        return value != null && value.isEnabled();
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }
}
