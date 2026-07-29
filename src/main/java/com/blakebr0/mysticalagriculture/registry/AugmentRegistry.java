package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.item.AugmentItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AugmentRegistry implements IAugmentRegistry {
    private static final AugmentRegistry INSTANCE = new AugmentRegistry();

    private final Map<Identifier, Augment> augments = new LinkedHashMap<>();
    private final Map<Identifier, String> sources = new LinkedHashMap<>();
    private String currentSourceMod;
    private boolean finalized;

    AugmentRegistry() {
    }

    @Override
    public void register(Augment augment) {
        if (this.finalized) {
            throw new IllegalStateException("Cannot register augment %s after the augment registry was finalized".formatted(augment.getId()));
        }
        if (this.currentSourceMod == null) {
            throw new IllegalStateException("Cannot register augment %s outside a plug-in registration callback".formatted(augment.getId()));
        }
        if (this.augments.containsKey(augment.getId())) {
            throw duplicate(augment.getId(), this.currentSourceMod);
        }

        this.augments.put(augment.getId(), augment);
        this.sources.put(augment.getId(), this.currentSourceMod);
    }

    @Override
    public List<Augment> getAugments() {
        return List.copyOf(this.augments.values());
    }

    @Override
    public Augment getAugmentById(Identifier id) {
        return this.augments.get(id);
    }

    public void registerItems() {
        for (var augment : this.augments.values()) {
            var id = MysticalAgriculture.resource(augment.getNameWithSuffix("augment"));
            var source = this.sources.getOrDefault(augment.getId(), augment.getModId());
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                throw new IllegalStateException("Duplicate item id %s contributed by mod %s".formatted(id, source));
            }
            Registry.register(BuiltInRegistries.ITEM, id, new AugmentItem(id, augment));
        }
    }

    public static AugmentRegistry getInstance() {
        return INSTANCE;
    }

    public void onCommonSetup() {
        MysticalAgriculture.LOGGER.info("Loaded {} augments", this.augments.size());
    }

    void beginRegistration(String sourceMod) {
        if (this.finalized) {
            throw new IllegalStateException("The augment registry is finalized");
        }
        this.currentSourceMod = sourceMod;
    }

    void endRegistration() {
        this.currentSourceMod = null;
    }

    void finalizeRegistration() {
        if (this.finalized) {
            throw new IllegalStateException("The augment registry is already finalized");
        }
        this.finalized = true;
        this.onCommonSetup();
    }

    private static IllegalStateException duplicate(Identifier id, String sourceMod) {
        return new IllegalStateException("Duplicate augment id %s contributed by mod %s".formatted(id, sourceMod));
    }
}
