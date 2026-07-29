package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginRegistryTest {
    @Test
    void corePluginIsFirstAndFabricCandidateOrderIsPreserved() {
        var registry = registryWith(
                candidate("first", "first.Plugin", NoOpPlugin::new),
                candidate("second", "second.Plugin", NoOpPlugin::new)
        );

        registry.loadPlugins();

        assertEquals(List.of("mysticalagriculture", "first", "second"), registry.getPluginSources());
    }

    @Test
    void duplicateCropIdNamesTheIdAndSourceMod() {
        var duplicateId = id("shared_crop");
        var registry = registryWith(
                candidate("example", "example.Plugin", () -> new IMysticalAgriculturePlugin() {
                    @Override
                    public void onRegisterCrops(ICropRegistry crops) {
                        crops.register(crop(duplicateId));
                        crops.register(crop(duplicateId));
                    }
                })
        );

        registry.loadPlugins();
        var exception = assertThrows(IllegalStateException.class, registry::collectContent);

        assertContainsIdAndSource(exception, duplicateId, "example");
    }

    @Test
    void duplicateAugmentIdNamesTheIdAndSourceMod() {
        var duplicateId = id("shared_augment");
        var registry = registryWith(
                candidate("example", "example.Plugin", () -> new IMysticalAgriculturePlugin() {
                    @Override
                    public void onRegisterAugments(IAugmentRegistry augments) {
                        augments.register(augment(duplicateId));
                        augments.register(augment(duplicateId));
                    }
                })
        );

        registry.loadPlugins();
        var exception = assertThrows(IllegalStateException.class, registry::collectContent);

        assertContainsIdAndSource(exception, duplicateId, "example");
    }

    @Test
    void duplicateMobSoulIdNamesTheIdAndSourceMod() {
        var duplicateId = id("shared_soul");
        var registry = registryWith(
                candidate("example", "example.Plugin", () -> new IMysticalAgriculturePlugin() {
                    @Override
                    public void onRegisterMobSoulTypes(IMobSoulTypeRegistry souls) {
                        souls.register(soul(duplicateId, id("entity_one")));
                        souls.register(soul(duplicateId, id("entity_two")));
                    }
                })
        );

        registry.loadPlugins();
        var exception = assertThrows(IllegalStateException.class, registry::collectContent);

        assertContainsIdAndSource(exception, duplicateId, "example");
    }

    @Test
    void duplicateDerivedBlockIdNamesTheIdAndSourceMod() {
        var expectedId = Identifier.fromNamespaceAndPath("mysticalagriculture", "shared_crop");
        var registry = registryWith(
                candidate("first", "first.Plugin", () -> cropPlugin("first", "shared")),
                candidate("second", "second.Plugin", () -> cropPlugin("second", "shared"))
        );

        registry.loadPlugins();
        registry.collectContent();
        var exception = assertThrows(IllegalStateException.class,
                () -> registry.getCropRegistry().registerBlocks((id, block) -> { }));

        assertContainsIdAndSource(exception, expectedId, "second");
    }

    @Test
    void duplicateDerivedItemIdNamesTheIdAndSourceMod() {
        var expectedId = Identifier.fromNamespaceAndPath("mysticalagriculture", "shared_essence");
        var registry = registryWith(
                candidate("first", "first.Plugin", () -> itemOnlyCropPlugin("first", "shared")),
                candidate("second", "second.Plugin", () -> itemOnlyCropPlugin("second", "shared"))
        );

        registry.loadPlugins();
        registry.collectContent();
        var exception = assertThrows(IllegalStateException.class,
                () -> registry.getCropRegistry().registerItems((id, item) -> { }));

        assertContainsIdAndSource(exception, expectedId, "second");
    }

    @Test
    void entrypointFailureNamesModAndDefinitionAndDoesNotFinalize() {
        var registry = registryWith(candidate("broken", "broken.Plugin", () -> {
            throw new IllegalArgumentException("boom");
        }));

        var exception = assertThrows(IllegalStateException.class, registry::loadPlugins);

        assertTrue(exception.getMessage().contains("broken"));
        assertTrue(exception.getMessage().contains("broken.Plugin"));
        assertFalse(registry.isFinalized());
    }

    @Test
    void registrationAfterFinalizationFails() {
        var registry = registryWith();
        registry.loadPlugins();
        registry.collectContent();
        registry.finalizeContent();

        var exception = assertThrows(IllegalStateException.class,
                () -> registry.getAugmentRegistry().register(augment(id("late"))));

        assertTrue(exception.getMessage().contains("finalized"));
    }

    private static PluginRegistry registryWith(PluginRegistry.PluginCandidate... candidates) {
        return new PluginRegistry(
                () -> List.of(candidates),
                new NoOpPlugin(),
                new CropRegistry(),
                new AugmentRegistry(),
                new MobSoulTypeRegistry()
        );
    }

    private static PluginRegistry.PluginCandidate candidate(
            String modId,
            String definition,
            PluginRegistry.PluginFactory factory
    ) {
        return new PluginRegistry.PluginCandidate(modId, definition, factory);
    }

    private static IMysticalAgriculturePlugin cropPlugin(String namespace, String name) {
        return new IMysticalAgriculturePlugin() {
            @Override
            public void onRegisterCrops(ICropRegistry crops) {
                crops.register(crop(Identifier.fromNamespaceAndPath(namespace, name)));
            }
        };
    }

    private static IMysticalAgriculturePlugin itemOnlyCropPlugin(String namespace, String name) {
        return new IMysticalAgriculturePlugin() {
            @Override
            public void onRegisterCrops(ICropRegistry crops) {
                crops.register(crop(Identifier.fromNamespaceAndPath(namespace, name))
                        .setCropBlock(null, false)
                        .setSeedsItem(null, false));
            }
        };
    }

    private static Crop crop(Identifier id) {
        return new Crop(id, CropTier.ONE, CropType.RESOURCE, null);
    }

    private static Augment augment(Identifier id) {
        return new Augment(id, 1, EnumSet.of(AugmentType.ARMOR), 0xFFFFFF, 0x000000);
    }

    private static MobSoulType soul(Identifier id, Identifier entityId) {
        return new MobSoulType(id, entityId, 10, 0xFFFFFF);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    private static void assertContainsIdAndSource(Throwable exception, Identifier id, String source) {
        assertTrue(exception.getMessage().contains(id.toString()), exception.getMessage());
        assertTrue(exception.getMessage().contains(source), exception.getMessage());
    }

    private static final class NoOpPlugin implements IMysticalAgriculturePlugin {
    }
}
