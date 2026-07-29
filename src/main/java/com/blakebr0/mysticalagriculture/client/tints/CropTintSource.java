package com.blakebr0.mysticalagriculture.client.tints;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record CropTintSource(Identifier id, CropTintType tintType) implements ItemTintSource {
    public static final MapCodec<CropTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("id").forGetter(CropTintSource::id),
                    CropTintType.CODEC.fieldOf("tint_type").forGetter(CropTintSource::tintType)
            ).apply(builder, CropTintSource::new)
    );

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        var crop = CropRegistry.getInstance().getCropById(this.id);

        return switch (tintType) {
            case ESSENCE -> crop.getEssenceColor();
            case SEED -> crop.getSeedColor();
        };
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }

    public enum CropTintType implements StringRepresentable {
        ESSENCE("essence"),
        SEED("seed");

        public static final Codec<CropTintType> CODEC = StringRepresentable.fromEnum(CropTintType::values);

        private final String name;

        CropTintType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
