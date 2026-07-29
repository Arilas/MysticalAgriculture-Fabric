package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginRegistryTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

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
        MappedRegistry<Block> blocks = registry("derived_blocks");
        var registry = registryWith(
                candidate("first", "first.Plugin", () -> cropPlugin("first", "shared")),
                candidate("second", "second.Plugin", () -> cropPlugin("second", "shared"))
        );

        registry.loadPlugins();
        registry.collectContent();
        var exception = assertThrows(IllegalStateException.class,
                () -> registry.getCropRegistry().registerBlocks(
                        (id, crop) -> (CropBlock) Blocks.WHEAT,
                        new DirectRegistryRegistrar<>(blocks, "block")
                ));

        assertContainsIdAndSource(exception, expectedId, "second");
        assertTrue(blocks.containsKey(expectedId), "the first derived block must pass through Registry.register");
    }

    @Test
    void duplicateDerivedItemIdNamesTheIdAndSourceMod() {
        var expectedId = Identifier.fromNamespaceAndPath("mysticalagriculture", "shared_essence");
        MappedRegistry<Item> items = registry("derived_items");
        var registry = registryWith(
                candidate("first", "first.Plugin", () -> itemOnlyCropPlugin("first", "shared")),
                candidate("second", "second.Plugin", () -> itemOnlyCropPlugin("second", "shared"))
        );

        registry.loadPlugins();
        registry.collectContent();
        var exception = assertThrows(IllegalStateException.class,
                () -> registry.getCropRegistry().registerItems(
                        PluginRegistryTest::item,
                        PluginRegistryTest::item,
                        new DirectRegistryRegistrar<>(items, "item")
                ));

        assertContainsIdAndSource(exception, expectedId, "second");
        assertTrue(items.containsKey(expectedId), "the first derived item must pass through Registry.register");
    }

    @Test
    void dynamicCropItemsPreserveUpstreamEssenceThenSeedOrder() {
        var registry = registryWith(candidate("example", "example.Plugin", () -> new IMysticalAgriculturePlugin() {
            @Override
            public void onRegisterCrops(ICropRegistry crops) {
                crops.register(crop(id("first")));
                crops.register(crop(id("second")));
            }
        }));
        registry.loadPlugins();
        registry.collectContent();
        MappedRegistry<Item> items = registry("ordered_crop_items");

        registry.getCropRegistry().registerItems(
                (id, _) -> id.getPath().startsWith("first") ? Items.STONE : Items.DIRT,
                (id, _) -> id.getPath().startsWith("first") ? Items.WHEAT_SEEDS : Items.BEETROOT_SEEDS,
                new DirectRegistryRegistrar<>(items, "item")
        );
        items.freeze();

        assertEquals(List.of(
                MysticalAgricultureAPI.resource("first_essence"),
                MysticalAgricultureAPI.resource("second_essence"),
                MysticalAgricultureAPI.resource("first_seeds"),
                MysticalAgricultureAPI.resource("second_seeds")
        ), IntStream.range(0, items.size()).mapToObj(items::byId).map(items::getKey).toList());
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
        assertTrue(registry.isFailed());
        assertThrows(IllegalStateException.class, registry::collectContent);
    }

    @Test
    void fatalVmErrorsEscapePluginLoadingUnwrapped() {
        var fatal = new OutOfMemoryError("fatal");
        var registry = registryWith(candidate("broken", "broken.Plugin", () -> {
            throw fatal;
        }));

        assertSame(fatal, assertThrows(OutOfMemoryError.class, registry::loadPlugins));
        assertFalse(registry.isFailed(), "an unrecoverable VM error must not be converted into registry state");
    }

    @Test
    void configureFailureNamesModAndDefinitionAndPoisonsLifecycle() {
        var registry = registryWith(candidate("broken_config", "broken.ConfigPlugin", () -> new NoOpPlugin() {
            @Override
            public void configure(com.blakebr0.mysticalagriculture.api.lib.PluginConfig config) {
                throw new IllegalArgumentException("configure boom");
            }
        }));

        var exception = assertThrows(IllegalStateException.class, registry::loadPlugins);

        assertPhaseFailure(exception, "broken_config", "broken.ConfigPlugin", "configure boom");
        assertTrue(registry.isFailed());
        assertThrows(IllegalStateException.class, registry::collectContent);
    }

    @Test
    void registrationFailureNamesModAndDefinitionAndPoisonsLifecycle() {
        var registry = registryWith(candidate("broken_register", "broken.RegisterPlugin", () -> new NoOpPlugin() {
            @Override
            public void onRegisterCrops(ICropRegistry crops) {
                throw new IllegalArgumentException("registration boom");
            }
        }));
        registry.loadPlugins();

        var exception = assertThrows(IllegalStateException.class, registry::collectContent);

        assertPhaseFailure(exception, "broken_register", "broken.RegisterPlugin", "registration boom");
        assertTrue(registry.isFailed());
        assertThrows(IllegalStateException.class, registry::finalizeContent);
    }

    @Test
    void postRegistrationFailureNamesModAndDefinitionAndPoisonsLifecycle() {
        var registry = registryWith(candidate("broken_post", "broken.PostPlugin", () -> new NoOpPlugin() {
            @Override
            public void onPostRegisterAugments(IAugmentRegistry augments) {
                throw new IllegalArgumentException("post boom");
            }
        }));
        registry.loadPlugins();
        registry.collectContent();

        var exception = assertThrows(IllegalStateException.class, registry::finalizeContent);

        assertPhaseFailure(exception, "broken_post", "broken.PostPlugin", "post boom");
        assertTrue(registry.isFailed());
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

    @Test
    void directRegistrarUsesVanillaRegistryCollisionAndFreezeBoundaries() {
        var blocks = PluginRegistryTest.<Block>registry("direct_blocks");
        var registrar = new DirectRegistryRegistrar<>(blocks, "block");
        var duplicateId = id("registered_block");
        registrar.register(duplicateId, block(duplicateId), "first_mod");

        var duplicate = assertThrows(IllegalStateException.class,
                () -> registrar.register(duplicateId, block(duplicateId), "second_mod"));
        assertContainsIdAndSource(duplicate, duplicateId, "second_mod");

        blocks.freeze();
        var frozen = assertThrows(IllegalStateException.class,
                () -> registrar.register(id("late_block"), Blocks.DIRT, "late_mod"));
        assertTrue(frozen.getMessage().toLowerCase().contains("frozen"), frozen.getMessage());
    }

    @Test
    void menuFactoryBuildsUsableFabricType() {
        var menuType = RegistryTypeFactories.extendedMenu(
                (syncId, inventory, pos) -> new TestMenu(syncId, pos),
                BlockPos.STREAM_CODEC
        );
        assertSame(BlockPos.STREAM_CODEC, menuType.getStreamCodec());
        var menu = menuType.create(7, null, BlockPos.ZERO);
        assertEquals(7, menu.containerId);
        assertEquals(BlockPos.ZERO, menu.pos);
    }

    @Test
    void blockEntityFactoryExposesTheVanillaFactoryBoundary() throws ReflectiveOperationException {
        var factory = RegistryTypeFactories.class.getMethod(
                "blockEntity",
                BlockEntityType.BlockEntitySupplier.class,
                Block[].class
        );

        assertEquals(BlockEntityType.class, factory.getReturnType());
        assertTrue(java.lang.reflect.Modifier.isStatic(factory.getModifiers()));
    }

    @Test
    void apiBootstrapRejectsDoubleInitialization() {
        MysticalAgricultureAPI.bootstrap(null, null, null, null);

        var exception = assertThrows(IllegalStateException.class,
                () -> MysticalAgricultureAPI.bootstrap(null, null, null, null));

        assertTrue(exception.getMessage().contains("already been initialized"));
    }

    @Test
    void apiCompatibilityClasspathCannotCompileImplementationImports() {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var source = new StringSource(
                "consumer.ImplementationLeak",
                "package consumer; import com.blakebr0.mysticalagriculture.init.ModItems; "
                        + "final class ImplementationLeak { Object value = ModItems.INFERIUM_ESSENCE; }"
        );
        var classpath = System.getProperty("apiCompatibilityClasspath");

        var success = compiler.getTask(
                null,
                null,
                diagnostics,
                List.of("-proc:none", "-classpath", classpath),
                null,
                List.of(source)
        ).call();

        assertFalse(success, "the API compatibility classpath must not expose implementation classes");
        assertTrue(diagnostics.getDiagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.getMessage(null).contains("does not exist")
                                || diagnostic.getMessage(null).contains("cannot find symbol")),
                diagnostics.getDiagnostics().toString());
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

    private static Block block(Identifier id) {
        return Blocks.STONE;
    }

    private static Item item(Identifier id, Crop crop) {
        return Items.STONE;
    }

    private static <T> MappedRegistry<T> registry(String path) {
        return new MappedRegistry<>(
                ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("task2_test", path)),
                Lifecycle.stable()
        );
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    private static void assertPhaseFailure(Throwable exception, String source, String definition, String cause) {
        assertTrue(exception.getMessage().contains(source), exception.getMessage());
        assertTrue(exception.getMessage().contains(definition), exception.getMessage());
        assertTrue(exception.getMessage().contains(cause), exception.getMessage());
    }

    private static void assertContainsIdAndSource(Throwable exception, Identifier id, String source) {
        assertTrue(exception.getMessage().contains(id.toString()), exception.getMessage());
        assertTrue(exception.getMessage().contains(source), exception.getMessage());
    }

    private static class NoOpPlugin implements IMysticalAgriculturePlugin {
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private final BlockPos pos;

        private TestMenu(int syncId, BlockPos pos) {
            super(null, syncId);
            this.pos = pos;
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }

        @Override
        public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int slot) {
            return ItemStack.EMPTY;
        }
    }

    private static final class StringSource extends SimpleJavaFileObject {
        private final String source;

        private StringSource(String className, String source) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return this.source;
        }
    }
}
