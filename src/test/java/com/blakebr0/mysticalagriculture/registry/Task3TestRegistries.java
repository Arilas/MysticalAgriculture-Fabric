package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureConfigValues;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureDataComponentTypes;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.blakebr0.mysticalagriculture.crafting.condition.AugmentEnabledCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.CropEnabledCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.CropHasMaterialCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.SeedCraftingRecipesEnabledCondition;
import com.blakebr0.mysticalagriculture.init.ModRecipeSerializers;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;
import java.util.List;
import java.util.LinkedHashMap;

public final class Task3TestRegistries {
    public static final Identifier CROP_ID = Identifier.fromNamespaceAndPath("testing", "component_crop");
    public static final Identifier TAG_CROP_ID = Identifier.fromNamespaceAndPath("testing", "tag_crop");
    public static final Identifier AUGMENT_ID = Identifier.fromNamespaceAndPath("testing", "enabled_augment");
    public static final Identifier SOUL_ID = Identifier.fromNamespaceAndPath("testing", "test_soul");
    public static final ResourceKey<Item> SOUL_JAR = ResourceKey.create(
            Registries.ITEM,
            MysticalAgricultureAPI.resource("soul_jar")
    );

    private static boolean initialized;
    private static HolderLookup.Provider lookup;
    private static RegistryAccess registryAccess;
    private static Registry<RecipeSerializer<?>> recipeSerializers;

    public static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var loaderTransformed = Boolean.getBoolean("fabric.unitTest");
        if (loaderTransformed && !BuiltInRegistries.ITEM.containsKey(SOUL_JAR)) {
            Registry.register(
                    BuiltInRegistries.ITEM,
                    SOUL_JAR,
                    new Item(new Item.Properties().setId(SOUL_JAR))
            );
        }

        registerDefaultResourceConditions();
        registerConditionType(CropEnabledCondition.TYPE);
        registerConditionType(AugmentEnabledCondition.TYPE);
        registerConditionType(CropHasMaterialCondition.TYPE);
        registerConditionType(SeedCraftingRecipesEnabledCondition.TYPE);
        if (loaderTransformed) {
            ModRecipeSerializers.register();
            recipeSerializers = BuiltInRegistries.RECIPE_SERIALIZER;
        } else {
            recipeSerializers = new MappedRegistry<>(Registries.RECIPE_SERIALIZER, Lifecycle.stable());
            ModRecipeSerializers.register(recipeSerializers);
            recipeSerializers.freeze();
        }
        registerPluginContent();

        var vanilla = VanillaRegistries.createLookup();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(vanilla)
                .forEach(pending -> pending.apply());

        lookup = vanilla;
        registryAccess = createNetworkRegistryAccess(vanilla);
        initialized = true;
    }

    public static HolderLookup.Provider lookup() {
        ensureInitialized();
        return lookup;
    }

    public static RegistryAccess registryAccess() {
        ensureInitialized();
        return registryAccess;
    }

    public static Identifier serializerId(RecipeSerializer<?> serializer) {
        ensureInitialized();
        return recipeSerializers.getKey(serializer);
    }

    private static void registerDefaultResourceConditions() {
        var tag = net.minecraft.tags.TagKey.create(
                Registries.ITEM,
                Identifier.parse("c:gems/diamond")
        );
        registerConditionType(ResourceConditions.tagsPopulated(tag).getType());
        registerConditionType(ResourceConditions.not(ResourceConditions.tagsPopulated(tag)).getType());
        registerConditionType(ResourceConditions.and(
                ResourceConditions.tagsPopulated(tag),
                ResourceConditions.tagsPopulated(tag)
        ).getType());
    }

    private static void registerConditionType(ResourceConditionType<?> type) {
        if (ResourceConditions.getConditionType(type.id()) == null) {
            ResourceConditions.register(type);
        }
    }

    private static void registerPluginContent() {
        var cropRegistry = CropRegistry.getInstance();
        var augmentRegistry = AugmentRegistry.getInstance();
        var soulRegistry = MobSoulTypeRegistry.getInstance();
        var plugins = new PluginRegistry(
                List::of,
                new TestPlugin(),
                cropRegistry,
                augmentRegistry,
                soulRegistry
        );

        plugins.loadPlugins();
        plugins.collectContent();
        plugins.finalizeContent();
        MysticalAgricultureAPI.bootstrap(
                cropRegistry,
                augmentRegistry,
                soulRegistry,
                new MysticalAgricultureConfigValues(
                        () -> 0.2,
                        () -> 0.1,
                        () -> true,
                        () -> false,
                        () -> false,
                        () -> true
                )
        );
    }

    private static RegistryAccess createNetworkRegistryAccess(HolderLookup.Provider vanilla) {
        var protection = vanilla.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PROTECTION);
        var enchantments = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        enchantments.register(Enchantments.PROTECTION, protection.value(), RegistrationInfo.BUILT_IN);
        enchantments.freeze();

        var registries = new LinkedHashMap<ResourceKey<? extends Registry<?>>, Registry<?>>();
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                .registries()
                .forEach(entry -> registries.put(entry.key(), entry.value()));
        registries.put(Registries.ENCHANTMENT, enchantments);

        return new RegistryAccess() {
            @Override
            @SuppressWarnings("unchecked")
            public <E> java.util.Optional<Registry<E>> lookup(
                    ResourceKey<? extends Registry<? extends E>> registryKey
            ) {
                return java.util.Optional.ofNullable((Registry<E>) registries.get(registryKey));
            }

            @Override
            public java.util.stream.Stream<RegistryEntry<?>> registries() {
                return registries.entrySet().stream()
                        .map(entry -> new RegistryEntry(entry.getKey(), entry.getValue()));
            }
        };
    }

    private static final class TestPlugin implements IMysticalAgriculturePlugin {
        private final CropTier tier = new CropTier(
                Identifier.fromNamespaceAndPath("testing", "tier"),
                1,
                0x112233,
                net.minecraft.ChatFormatting.WHITE
        ).setEssenceItem(() -> Items.REDSTONE);
        private final CropType type = new CropType(
                Identifier.fromNamespaceAndPath("testing", "type"),
                Identifier.fromNamespaceAndPath("testing", "crop")
        ).setCraftingSeedItem(() -> Items.WHEAT_SEEDS);

        @Override
        public void onRegisterCrops(ICropRegistry crops) {
            crops.registerTier(this.tier);
            crops.registerType(this.type);
            crops.register(new Crop(
                    CROP_ID,
                    this.tier,
                    this.type,
                    LazyIngredient.item(
                            "minecraft:diamond",
                            DataComponentMap.builder()
                                    .set(DataComponents.CUSTOM_NAME, Component.literal("required material"))
                                    .build()
                    )
            ).setCropBlock(() -> (net.minecraft.world.level.block.CropBlock) Blocks.WHEAT, true)
                    .setEssenceItem(() -> Items.REDSTONE, true)
                    .setSeedsItem(() -> Items.WHEAT_SEEDS, true));
            crops.register(new Crop(
                    TAG_CROP_ID,
                    this.tier,
                    this.type,
                    LazyIngredient.tag("c:gems/diamond")
            ).setCropBlock(() -> (net.minecraft.world.level.block.CropBlock) Blocks.BEETROOTS, true)
                    .setEssenceItem(() -> Items.BEETROOT, true)
                    .setSeedsItem(() -> Items.BEETROOT_SEEDS, true));
        }

        @Override
        public void onRegisterAugments(IAugmentRegistry augments) {
            augments.register(new Augment(
                    AUGMENT_ID,
                    1,
                    EnumSet.of(AugmentType.TOOL),
                    0x112233,
                    0x445566
            ) {
                @Override
                public Item getItem() {
                    return Items.STICK;
                }
            });
        }

        @Override
        public void onRegisterMobSoulTypes(IMobSoulTypeRegistry souls) {
            souls.register(new MobSoulType(
                    SOUL_ID,
                    Identifier.parse("minecraft:pig"),
                    4.0,
                    0x778899
            ));
        }
    }

    private Task3TestRegistries() {
    }
}
