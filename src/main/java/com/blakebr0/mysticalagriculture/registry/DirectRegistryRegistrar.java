package com.blakebr0.mysticalagriculture.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class DirectRegistryRegistrar<T> {
    private final Registry<T> registry;
    private final String kind;

    public DirectRegistryRegistrar(Registry<T> registry, String kind) {
        this.registry = registry;
        this.kind = kind;
    }

    public boolean contains(Identifier id) {
        return this.registry.containsKey(id);
    }

    public T register(Identifier id, T value, String sourceMod) {
        if (this.contains(id)) {
            throw new IllegalStateException(
                    "Duplicate %s id %s contributed by mod %s".formatted(this.kind, id, sourceMod)
            );
        }

        return Registry.register(this.registry, id, value);
    }
}
