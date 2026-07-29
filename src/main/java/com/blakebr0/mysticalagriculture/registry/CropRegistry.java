package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.PluginConfig;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CropRegistry implements ICropRegistry {
    private static final CropRegistry INSTANCE = new CropRegistry();
    private static final Logger LOGGER = LoggerFactory.getLogger("Mystical Agriculture");

    private Map<Identifier, Crop> crops = new LinkedHashMap<>();
    private final Map<Identifier, CropTier> tiers = new LinkedHashMap<>();
    private final Map<Identifier, CropType> types = new LinkedHashMap<>();
    private final Map<Identifier, String> cropSources = new LinkedHashMap<>();
    private String currentSourceMod;
    private PluginConfig currentPluginConfig;
    private boolean finalized;

    CropRegistry() {
    }

    @Override
    public void register(Crop crop) {
        this.requireRegistration("crop", crop.getId());
        this.putUnique(this.crops, crop.getId(), crop, "crop");
        this.cropSources.put(crop.getId(), this.currentSourceMod);
        this.loadRecipeConfig(crop);
    }

    @Override
    public void registerTier(CropTier tier) {
        this.requireRegistration("crop tier", tier.getId());
        this.putUnique(this.tiers, tier.getId(), tier, "crop tier");
    }

    @Override
    public void registerType(CropType type) {
        this.requireRegistration("crop type", type.getId());
        this.putUnique(this.types, type.getId(), type, "crop type");
    }

    @Override
    public List<Crop> getCrops() {
        return List.copyOf(this.crops.values());
    }

    @Override
    public Crop getCropById(Identifier id) {
        return this.crops.get(id);
    }

    @Override
    public Crop getCropByName(String name) {
        return this.crops.values().stream().filter(c -> name.equals(c.getName())).findFirst().orElse(null);
    }

    @Override
    public List<CropTier> getTiers() {
        return List.copyOf(this.tiers.values());
    }

    @Override
    public CropTier getTierById(Identifier id) {
        return this.tiers.get(id);
    }

    @Override
    public List<CropType> getTypes() {
        return List.copyOf(this.types.values());
    }

    @Override
    public CropType getTypeById(Identifier id) {
        return this.types.get(id);
    }

    public void registerBlocks(BlockFactory factory, DirectRegistryRegistrar<Block> registrar) {
        var claimed = new HashSet<Identifier>();
        var crops = this.crops.values();

        for (var crop : crops) {
            if (!crop.shouldRegisterCropBlock()) {
                continue;
            }

            var id = MysticalAgricultureAPI.resource(crop.getNameWithSuffix("crop"));
            var source = this.sourceOf(crop);
            this.claimId(id, source, "block", claimed, registrar.contains(id));

            var block = crop.getCropBlock();
            if (block == null) {
                var defaultCrop = factory.create(id, crop);
                block = defaultCrop;
                crop.setCropBlock(() -> defaultCrop, true);
            }

            registrar.register(id, block, source);
        }

        this.crops = getSortedCropsMap(crops);
    }

    public void registerItems(
            ItemFactory essenceFactory,
            ItemFactory seedsFactory,
            DirectRegistryRegistrar<Item> registrar
    ) {
        var claimed = new HashSet<Identifier>();

        for (var crop : this.crops.values()) {
            if (crop.shouldRegisterEssenceItem()) {
                var id = MysticalAgricultureAPI.resource(crop.getNameWithSuffix("essence"));
                var source = this.sourceOf(crop);
                this.claimId(id, source, "item", claimed, registrar.contains(id));

                var item = crop.getEssenceItem();
                if (item == null) {
                    var defaultEssence = essenceFactory.create(id, crop);
                    item = defaultEssence;
                    crop.setEssenceItem(() -> defaultEssence, true);
                }

                registrar.register(id, item, source);
            }

            if (crop.shouldRegisterSeedsItem()) {
                var id = MysticalAgricultureAPI.resource(crop.getNameWithSuffix("seeds"));
                var source = this.sourceOf(crop);
                this.claimId(id, source, "item", claimed, registrar.contains(id));

                var item = crop.getSeedsItem();
                if (item == null) {
                    var defaultSeeds = seedsFactory.create(id, crop);
                    item = defaultSeeds;
                    crop.setSeedsItem(() -> defaultSeeds, true);
                }

                registrar.register(id, item, source);
            }
        }
    }

    public static CropRegistry getInstance() {
        return INSTANCE;
    }

    public void onCommonSetup() {
        LOGGER.info("Loaded {} crops", this.crops.size());
        LOGGER.info("Loaded {} crop tiers", this.tiers.size());
        LOGGER.info("Loaded {} crop types", this.types.size());
    }

    void beginRegistration(String sourceMod, PluginConfig config) {
        if (this.finalized) {
            throw new IllegalStateException("The crop registry is finalized");
        }
        this.currentSourceMod = sourceMod;
        this.currentPluginConfig = config;
    }

    void endRegistration() {
        this.currentSourceMod = null;
        this.currentPluginConfig = null;
    }

    void finalizeRegistration() {
        if (this.finalized) {
            throw new IllegalStateException("The crop registry is already finalized");
        }
        this.finalized = true;
        this.onCommonSetup();
    }

    private void requireRegistration(String kind, Identifier id) {
        if (this.finalized) {
            throw new IllegalStateException("Cannot register %s %s after the crop registry was finalized".formatted(kind, id));
        }
        if (this.currentSourceMod == null) {
            throw new IllegalStateException("Cannot register %s %s outside a plug-in registration callback".formatted(kind, id));
        }
    }

    private <T> void putUnique(Map<Identifier, T> values, Identifier id, T value, String kind) {
        if (values.containsKey(id)) {
            throw duplicate(kind, id, this.currentSourceMod);
        }
        values.put(id, value);
    }

    private void claimId(
            Identifier id,
            String sourceMod,
            String kind,
            Set<Identifier> claimed,
            boolean alreadyRegistered
    ) {
        if (alreadyRegistered || !claimed.add(id)) {
            throw duplicate(kind, id, sourceMod);
        }
    }

    private String sourceOf(Crop crop) {
        return this.cropSources.getOrDefault(crop.getId(), crop.getModId());
    }

    private static IllegalStateException duplicate(String kind, Identifier id, String sourceMod) {
        return new IllegalStateException("Duplicate %s id %s contributed by mod %s".formatted(kind, id, sourceMod));
    }

    private void loadRecipeConfig(Crop crop) {
        var recipes = crop.getRecipeConfig();
        var config = this.currentPluginConfig;

        recipes.setSeedCraftingRecipeEnabled(
                recipes.isSeedCraftingRecipeEnabled() && config.isDynamicSeedCraftingRecipesEnabled()
        );
        recipes.setSeedInfusionRecipeEnabled(
                recipes.isSeedInfusionRecipeEnabled() && config.isDynamicSeedInfusionRecipesEnabled()
        );
        recipes.setSeedReprocessorRecipeEnabled(
                recipes.isSeedReprocessorRecipeEnabled() && config.isDynamicSeedReprocessorRecipesEnabled()
        );
    }

    private static Map<Identifier, Crop> getSortedCropsMap(Collection<Crop> crops) {
        var sorted = new LinkedHashMap<Identifier, Crop>();
        crops.stream()
                .sorted(Comparator.comparingInt(c -> c.getTier().getValue()))
                .forEach(c -> sorted.put(c.getId(), c));
        return sorted;
    }

    @FunctionalInterface
    public interface BlockFactory {
        CropBlock create(Identifier id, Crop crop);
    }

    @FunctionalInterface
    public interface ItemFactory {
        Item create(Identifier id, Crop crop);
    }
}
