package com.blakebr0.mysticalagriculture.crafting.ingredient;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.Objects;
import java.util.stream.Stream;

public final class CropComponentIngredient implements CustomIngredient {
    public static final MapCodec<CropComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("crop").forGetter(CropComponentIngredient::crop),
                    ComponentType.CODEC.fieldOf("component").forGetter(CropComponentIngredient::componentType)
            ).apply(builder, CropComponentIngredient::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CropComponentIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC,
                    CropComponentIngredient::crop,
                    ByteBufCodecs.STRING_UTF8.map(ComponentType::fromName, ComponentType::getSerializedName),
                    CropComponentIngredient::componentType,
                    CropComponentIngredient::new
            );
    public static final CustomIngredientSerializer<CropComponentIngredient> SERIALIZER =
            new CustomIngredientSerializer<>() {
                @Override
                public Identifier getIdentifier() {
                    return CustomIngredientMatcher.CROP_COMPONENT_ID;
                }

                @Override
                public MapCodec<CropComponentIngredient> getCodec() {
                    return CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, CropComponentIngredient> getStreamCodec() {
                    return STREAM_CODEC;
                }
            };

    private final Identifier crop;
    private final ComponentType type;
    private HolderSet<Item> values;
    private DataComponentPatch components = DataComponentPatch.EMPTY;

    public CropComponentIngredient(Identifier crop, ComponentType type) {
        this.crop = crop;
        this.type = type;
    }

    @Override
    public boolean test(ItemStack input) {
        this.items();
        return CustomIngredientMatcher.matchesComponents(input, this.values.stream(), this.components);
    }

    @Override
    public Stream<Holder<Item>> items() {
        if (this.values == null) {
            var crop = CropRegistry.getInstance().getCropById(this.crop);
            if (crop == null) {
                this.values = HolderSet.empty();
                return this.values.stream();
            }

            this.values = switch (this.type) {
                case ESSENCE -> holderSet(crop.getTier().getEssenceItem());
                case SEED -> holderSet(crop.getType().getCraftingSeedItem());
                case MATERIAL -> {
                    var ingredient = crop.getLazyIngredient();
                    var ingredientComponents = ingredient.getComponents();
                    if (!ingredientComponents.isEmpty()) {
                        var builder = DataComponentPatch.builder();
                        for (var component : ingredientComponents) {
                            builder.set(component);
                        }
                        this.components = builder.build();
                    }
                    if (ingredient.isTag()) {
                        yield BuiltInRegistries.ITEM.get(
                                TagKey.create(Registries.ITEM, Identifier.parse(ingredient.getId()))
                        ).<HolderSet<Item>>map(value -> value).orElseGet(HolderSet::empty);
                    }
                    if (ingredient.isItem()) {
                        yield BuiltInRegistries.ITEM.get(Identifier.parse(ingredient.getId()))
                                .<HolderSet<Item>>map(value -> HolderSet.direct(value))
                                .orElseGet(HolderSet::empty);
                    }
                    yield HolderSet.empty();
                }
            };
        }

        return this.values.stream();
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(this.items().<SlotDisplay>map(item -> {
                    var template = new ItemStackTemplate(item, 1, this.components);
                    var display = new SlotDisplay.ItemStackSlotDisplay(template);
                    var remainder = item.value().getCraftingRemainder();
                    if (remainder != null) {
                        return new SlotDisplay.WithRemainder(
                                display,
                                new SlotDisplay.ItemStackSlotDisplay(remainder)
                        );
                    }
                    return display;
                })
                .toList());
    }

    public static Ingredient of(Identifier crop, ComponentType type) {
        return new CropComponentIngredient(crop, type).toVanilla();
    }

    private Identifier crop() {
        return this.crop;
    }

    private ComponentType componentType() {
        return this.type;
    }

    private static HolderSet<Item> holderSet(Item item) {
        return item == null ? HolderSet.empty() : HolderSet.direct(item.builtInRegistryHolder());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof CropComponentIngredient ingredient
                && this.crop.equals(ingredient.crop)
                && this.type == ingredient.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.crop, this.type);
    }

    public enum ComponentType implements StringRepresentable {
        ESSENCE("essence"),
        SEED("seed"),
        MATERIAL("material");

        public static final Codec<ComponentType> CODEC = StringRepresentable.fromEnum(ComponentType::values);
        private final String name;

        ComponentType(String name) {
            this.name = name;
        }

        private static ComponentType fromName(String name) {
            for (var value : values()) {
                if (value.name.equals(name)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown crop component type: " + name);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
