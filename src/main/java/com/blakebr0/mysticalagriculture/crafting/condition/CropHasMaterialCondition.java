package com.blakebr0.mysticalagriculture.crafting.condition;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;

public record CropHasMaterialCondition(Identifier crop) implements ResourceCondition {
    public static final MapCodec<CropHasMaterialCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("crop").forGetter(CropHasMaterialCondition::crop)
            ).apply(builder, CropHasMaterialCondition::new)
    );
    public static final ResourceConditionType<CropHasMaterialCondition> TYPE =
            ResourceConditionType.create(
                    MysticalAgricultureAPI.resource("crop_has_material"),
                    CODEC
            );

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        var value = CropRegistry.getInstance().getCropById(this.crop);
        if (value == null) {
            return false;
        }

        var lazy = value.getLazyIngredient();
        if (lazy == null || lazy == com.blakebr0.mysticalagriculture.api.lib.LazyIngredient.EMPTY) {
            return false;
        }
        if (lazy.isItem()) {
            return BuiltInRegistries.ITEM.containsKey(Identifier.parse(lazy.getId()));
        }
        if (lazy.isTag() && registryInfo != null) {
            return registryInfo.lookup(Registries.ITEM)
                    .flatMap(info -> info.getter().get(
                            TagKey.create(Registries.ITEM, Identifier.parse(lazy.getId()))
                    ))
                    .isPresent();
        }
        return false;
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }
}
