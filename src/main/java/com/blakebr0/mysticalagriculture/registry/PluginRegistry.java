package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.lib.PluginConfig;
import com.blakebr0.mysticalagriculture.lib.ModCorePlugin;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class PluginRegistry {
    public static final String ENTRYPOINT_KEY = "mysticalagriculture:plugin";

    private static final PluginRegistry INSTANCE = new PluginRegistry(
            PluginRegistry::getFabricCandidates,
            new ModCorePlugin(),
            CropRegistry.getInstance(),
            AugmentRegistry.getInstance(),
            MobSoulTypeRegistry.getInstance()
    );

    private final PluginSource pluginSource;
    private final IMysticalAgriculturePlugin corePlugin;
    private final CropRegistry cropRegistry;
    private final AugmentRegistry augmentRegistry;
    private final MobSoulTypeRegistry mobSoulTypeRegistry;
    private final List<LoadedPlugin> plugins = new ArrayList<>();
    private State state = State.NEW;

    PluginRegistry(
            PluginSource pluginSource,
            IMysticalAgriculturePlugin corePlugin,
            CropRegistry cropRegistry,
            AugmentRegistry augmentRegistry,
            MobSoulTypeRegistry mobSoulTypeRegistry
    ) {
        this.pluginSource = pluginSource;
        this.corePlugin = corePlugin;
        this.cropRegistry = cropRegistry;
        this.augmentRegistry = augmentRegistry;
        this.mobSoulTypeRegistry = mobSoulTypeRegistry;
    }

    public void loadPlugins() {
        this.requireState(State.NEW, "load plug-ins");

        this.loadPlugin(new PluginCandidate(
                MysticalAgriculture.MOD_ID,
                ModCorePlugin.class.getName(),
                () -> this.corePlugin
        ));

        for (var candidate : this.pluginSource.getCandidates()) {
            this.loadPlugin(candidate);
        }

        this.state = State.LOADED;
        MysticalAgriculture.LOGGER.info("Loaded {} Mystical Agriculture plug-ins", this.plugins.size());
    }

    public void collectContent() {
        this.requireState(State.LOADED, "collect plug-in content");

        for (var loaded : this.plugins) {
            this.invoke(loaded, () -> {
                this.cropRegistry.beginRegistration(loaded.sourceMod(), loaded.config());
                this.augmentRegistry.beginRegistration(loaded.sourceMod());
                this.mobSoulTypeRegistry.beginRegistration(loaded.sourceMod());

                try {
                    loaded.plugin().onRegisterCrops(this.cropRegistry);
                    loaded.plugin().onRegisterAugments(this.augmentRegistry);
                    loaded.plugin().onRegisterMobSoulTypes(this.mobSoulTypeRegistry);
                } finally {
                    this.cropRegistry.endRegistration();
                    this.augmentRegistry.endRegistration();
                    this.mobSoulTypeRegistry.endRegistration();
                }
            });
        }

        this.state = State.COLLECTED;
    }

    public void finalizeContent() {
        this.requireState(State.COLLECTED, "finalize plug-in content");

        for (var loaded : this.plugins) {
            this.invoke(loaded, () -> {
                this.cropRegistry.beginRegistration(loaded.sourceMod(), loaded.config());
                this.augmentRegistry.beginRegistration(loaded.sourceMod());
                this.mobSoulTypeRegistry.beginRegistration(loaded.sourceMod());

                try {
                    loaded.plugin().onPostRegisterCrops(this.cropRegistry);
                    loaded.plugin().onPostRegisterAugments(this.augmentRegistry);
                    loaded.plugin().onPostRegisterMobSoulTypes(this.mobSoulTypeRegistry);
                } finally {
                    this.cropRegistry.endRegistration();
                    this.augmentRegistry.endRegistration();
                    this.mobSoulTypeRegistry.endRegistration();
                }
            });
        }

        this.cropRegistry.finalizeRegistration();
        this.augmentRegistry.finalizeRegistration();
        this.mobSoulTypeRegistry.finalizeRegistration();
        this.state = State.FINALIZED;
    }

    public void forEach(BiConsumer<IMysticalAgriculturePlugin, PluginConfig> action) {
        this.plugins.forEach(plugin -> action.accept(plugin.plugin(), plugin.config()));
    }

    public boolean isFinalized() {
        return this.state == State.FINALIZED;
    }

    List<String> getPluginSources() {
        return this.plugins.stream().map(LoadedPlugin::sourceMod).toList();
    }

    CropRegistry getCropRegistry() {
        return this.cropRegistry;
    }

    AugmentRegistry getAugmentRegistry() {
        return this.augmentRegistry;
    }

    MobSoulTypeRegistry getMobSoulTypeRegistry() {
        return this.mobSoulTypeRegistry;
    }

    public static PluginRegistry getInstance() {
        return INSTANCE;
    }

    private void loadPlugin(PluginCandidate candidate) {
        try {
            var plugin = candidate.factory().create();
            var config = new PluginConfig();
            plugin.configure(config);
            this.plugins.add(new LoadedPlugin(candidate.sourceMod(), candidate.definition(), plugin, config));
            MysticalAgriculture.LOGGER.info(
                    "Registered Mystical Agriculture plug-in {} from {}",
                    candidate.definition(),
                    candidate.sourceMod()
            );
        } catch (Throwable e) {
            this.state = State.FAILED;
            throw entrypointFailure(candidate.sourceMod(), candidate.definition(), e);
        }
    }

    private void invoke(LoadedPlugin plugin, Runnable action) {
        try {
            action.run();
        } catch (Throwable e) {
            this.state = State.FAILED;
            throw entrypointFailure(plugin.sourceMod(), plugin.definition(), e);
        }
    }

    private void requireState(State expected, String action) {
        if (this.state != expected) {
            throw new IllegalStateException("Cannot %s while plug-in registry state is %s".formatted(action, this.state));
        }
    }

    private static IllegalStateException entrypointFailure(String sourceMod, String definition, Throwable cause) {
        return new IllegalStateException(
                "Mystical Agriculture plug-in entrypoint %s from mod %s failed: %s"
                        .formatted(definition, sourceMod, cause.getMessage()),
                cause
        );
    }

    private static List<PluginCandidate> getFabricCandidates() {
        return FabricLoader.getInstance()
                .getEntrypointContainers(ENTRYPOINT_KEY, IMysticalAgriculturePlugin.class)
                .stream()
                .map(container -> new PluginCandidate(
                        container.getProvider().getMetadata().getId(),
                        container.getDefinition(),
                        container::getEntrypoint
                ))
                .toList();
    }

    @FunctionalInterface
    interface PluginSource {
        List<PluginCandidate> getCandidates();
    }

    @FunctionalInterface
    interface PluginFactory {
        IMysticalAgriculturePlugin create();
    }

    record PluginCandidate(String sourceMod, String definition, PluginFactory factory) {
    }

    private record LoadedPlugin(
            String sourceMod,
            String definition,
            IMysticalAgriculturePlugin plugin,
            PluginConfig config
    ) {
    }

    private enum State {
        NEW,
        LOADED,
        COLLECTED,
        FINALIZED,
        FAILED
    }
}
