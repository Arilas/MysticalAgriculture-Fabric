package com.blakebr0.mysticalagriculture.compat;

import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IOreInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount;
import com.blakebr0.mysticalagriculture.compat.jei.JeiCompat;
import com.blakebr0.mysticalagriculture.compat.jei.recipe.CruxRecipe;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModRecipeSerializers;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OptionalIntegrationApiTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        Task7TestBootstrap.ensureInitialized();
    }

    @Test
    void pluginsImplementThePublishedFabricDiscoveryApis() {
        assertInstanceOf(IModPlugin.class, new JeiCompat());
        assertInstanceOf(IWailaPlugin.class, new JadeCompat());
        assertNotNull(JadeCompat.class.getAnnotation(WailaPlugin.class));
    }

    @Test
    void everyMysticalAgricultureRecipeSerializerSynchronizesToFabricClients() {
        assertEquals(List.of(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
        ), List.of(
                RecipeSyncImpl.isSynced(ModRecipeSerializers.CRAFTING_FARMLAND_TILL),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.INFUSION),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.AWAKENING),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.ENCHANTER),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.REPROCESSOR),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.SOUL_EXTRACTION),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.SOULIUM_SPAWNER),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.ORE_INFUSION),
                RecipeSyncImpl.isSynced(ModRecipeSerializers.CRAFTING_SOUL_JAR_EMPTY)
        ));
    }

    @Test
    void jeiRegistersEveryCategoryCatalystRecipeClickAreaAndSubtype() {
        var plugin = new JeiCompat();
        var categories = new ArrayList<IRecipeCategory<?>>();
        var catalysts = new ArrayList<Catalyst>();
        var recipes = new ArrayList<String>();
        var ingredientInfo = new ArrayList<String>();
        var clickAreas = new ArrayList<ClickArea>();
        var subtypeInterpreters = new ArrayList<ISubtypeInterpreter<ItemStack>>();
        var helpers = proxy(IJeiHelpers.class, (method, _) ->
                method.getName().equals("getGuiHelper") ? guiHelper() : null
        );

        plugin.registerCategories(proxy(IRecipeCategoryRegistration.class, (method, args) -> {
            if (method.getName().equals("getJeiHelpers"))
                return helpers;
            if (method.getName().equals("addRecipeCategories")) {
                Collections.addAll(categories, (IRecipeCategory<?>[]) args[0]);
            }
            return null;
        }));
        plugin.registerRecipeCatalysts(proxy(IRecipeCatalystRegistration.class, (method, args) -> {
            if (method.getName().equals("addCraftingStation") && args.length == 2) {
                var type = (IRecipeType<?>) args[0];
                var stations = args[1];
                for (int index = 0; index < Array.getLength(stations); index++) {
                    var stack = (ItemStack) Array.get(stations, index);
                    catalysts.add(new Catalyst(type.getUid().toString(), itemId(stack)));
                }
            }
            return null;
        }));
        plugin.registerRecipes(proxy(IRecipeRegistration.class, (method, args) -> {
            if (method.getName().equals("addRecipes")) {
                recipes.add(((IRecipeType<?>) args[0]).getUid().toString());
            } else if (method.getName().equals("addIngredientInfo") && args[0] instanceof ItemStack stack) {
                ingredientInfo.add(itemId(stack));
            }
            return null;
        }));
        plugin.registerGuiHandlers(proxy(IGuiHandlerRegistration.class, (method, args) -> {
            if (method.getName().equals("addRecipeClickArea")) {
                var types = (IRecipeType<?>[]) args[5];
                clickAreas.add(new ClickArea(
                        ((Class<?>) args[0]).getSimpleName(),
                        (int) args[1],
                        (int) args[2],
                        (int) args[3],
                        (int) args[4],
                        types[0].getUid().toString()
                ));
            }
            return null;
        }));
        plugin.registerItemSubtypes(proxy(ISubtypeRegistration.class, (method, args) -> {
            if (method.getName().equals("registerSubtypeInterpreter")) {
                subtypeInterpreters.add(castSubtype(args[2]));
            }
            return null;
        }));

        assertEquals(List.of(
                "InfusionCategory",
                "AwakeningCategory",
                "EnchanterCategory",
                "ReprocessorCategory",
                "SoulExtractorCategory",
                "SouliumSpawnerCategory",
                "OreInfuserCategory",
                "CruxCategory"
        ), categories.stream().map(category -> category.getClass().getSimpleName()).toList());
        assertEquals(List.of(
                "mysticalagriculture:infusion",
                "mysticalagriculture:awakening",
                "mysticalagriculture:enchanter",
                "mysticalagriculture:reprocessor",
                "mysticalagriculture:soul_extractor",
                "mysticalagriculture:soulium_spawner",
                "mysticalagriculture:ore_infuser",
                "mysticalagriculture:crux"
        ), categories.stream().map(category -> category.getRecipeType().getUid().toString()).toList());
        assertEquals(List.of(
                new Catalyst("mysticalagriculture:infusion", "mysticalagriculture:infusion_altar"),
                new Catalyst("mysticalagriculture:infusion", "mysticalagriculture:infusion_pedestal"),
                new Catalyst("mysticalagriculture:awakening", "mysticalagriculture:awakening_altar"),
                new Catalyst("mysticalagriculture:awakening", "mysticalagriculture:awakening_pedestal"),
                new Catalyst("mysticalagriculture:awakening", "mysticalagriculture:essence_vessel"),
                new Catalyst("mysticalagriculture:enchanter", "mysticalagriculture:enchanter"),
                new Catalyst("minecraft:smelting", "mysticalagriculture:furnace"),
                new Catalyst("mysticalagriculture:reprocessor", "mysticalagriculture:seed_reprocessor"),
                new Catalyst("mysticalagriculture:soul_extractor", "mysticalagriculture:soul_extractor"),
                new Catalyst("mysticalagriculture:soulium_spawner", "mysticalagriculture:soulium_spawner"),
                new Catalyst("mysticalagriculture:ore_infuser", "mysticalagriculture:ore_infuser")
        ), catalysts);
        assertEquals(List.of(
                "mysticalagriculture:infusion",
                "mysticalagriculture:awakening",
                "mysticalagriculture:enchanter",
                "mysticalagriculture:reprocessor",
                "mysticalagriculture:soul_extractor",
                "mysticalagriculture:soulium_spawner",
                "mysticalagriculture:ore_infuser",
                "mysticalagriculture:crux"
        ), recipes);
        assertEquals(List.of("mysticalagriculture:cognizant_dust"), ingredientInfo);
        assertEquals(List.of(
                new ClickArea("EnchanterScreen", 104, 41, 22, 15, "mysticalagriculture:enchanter"),
                new ClickArea("EssenceFurnaceScreen", 99, 52, 22, 15, "minecraft:smelting"),
                new ClickArea("ReprocessorScreen", 99, 52, 22, 15, "mysticalagriculture:reprocessor"),
                new ClickArea("SoulExtractorScreen", 99, 52, 22, 15, "mysticalagriculture:soul_extractor"),
                new ClickArea("SouliumSpawnerScreen", 99, 52, 22, 15, "mysticalagriculture:soulium_spawner"),
                new ClickArea("OreInfuserScreen", 105, 52, 22, 15, "mysticalagriculture:ore_infuser")
        ), clickAreas);
        assertEquals(1, subtypeInterpreters.size());
        assertEquals("", subtypeInterpreters.getFirst().getSubtypeData(
                new ItemStack(ModItems.SOUL_JAR),
                UidContext.Ingredient
        ));
    }

    @Test
    void everyJeiCategoryBuildsItsCompleteProductionLayout() throws ReflectiveOperationException {
        var categories = categories();

        assertLayout(categories, "InfusionCategory", infusionRecipe(), List.of(
                input(33, 33), input(7, 7), input(33, 1), input(59, 7), input(65, 33),
                input(59, 59), input(33, 64), input(7, 59), input(1, 33), output(123, 33)
        ));
        assertLayout(categories, "AwakeningCategory", awakeningRecipe(), List.of(
                input(33, 33), input(7, 7), input(33, 1), input(59, 7), input(65, 33),
                input(59, 59), input(33, 64), input(7, 59), input(1, 33), output(123, 33)
        ));
        assertLayout(categories, "EnchanterCategory", enchanterRecipe(), List.of(
                input(1, 5), input(23, 5), input(63, 5), output(123, 5)
        ));
        assertLayout(categories, "ReprocessorCategory", reprocessorRecipe(), List.of(
                input(1, 5), output(61, 5)
        ));
        assertLayout(categories, "SoulExtractorCategory", soulExtractorRecipe(), List.of(
                input(1, 5), output(61, 5)
        ));
        assertLayout(categories, "SouliumSpawnerCategory", souliumSpawnerRecipe(), List.of(
                input(1, 5), output(61, 5)
        ));
        assertLayout(categories, "OreInfuserCategory", oreInfusionRecipe(), List.of(
                input(1, 5), input(21, 5), output(81, 5)
        ));

        var farmlands = CruxRecipe.class.getDeclaredField("farmlands");
        farmlands.setAccessible(true);
        farmlands.set(null, List.of(Blocks.FARMLAND));
        assertLayout(categories, "CruxCategory", new CruxRecipe(
                new ItemStack(Items.WHEAT_SEEDS),
                new ItemStack(Blocks.DIAMOND_BLOCK),
                new ItemStack(Items.DIAMOND)
        ), List.of(input(1, 1), input(1, 19), input(1, 37), output(59, 20)));
    }

    @Test
    void jeiRegistersTransfersForEveryMenuBackedRecipeCategory() {
        var transfers = new ArrayList<Transfer>();
        var registration = proxy(IRecipeTransferRegistration.class, (method, args) -> {
            if (method.getName().equals("addRecipeTransferHandler") && args.length == 7) {
                transfers.add(new Transfer(
                        ((Class<?>) args[0]).getSimpleName(),
                        ((IRecipeType<?>) args[2]).getUid().toString(),
                        (int) args[3],
                        (int) args[4],
                        (int) args[5],
                        (int) args[6]
                ));
            }
            return null;
        });

        new JeiCompat().registerRecipeTransferHandlers(registration);

        assertEquals(List.of(
                new Transfer("EnchanterContainer", "mysticalagriculture:enchanter", 0, 3, 4, 36),
                new Transfer("EssenceFurnaceContainer", "minecraft:smelting", 1, 1, 4, 36),
                new Transfer("ReprocessorContainer", "mysticalagriculture:reprocessor", 1, 1, 4, 36),
                new Transfer("SoulExtractorContainer", "mysticalagriculture:soul_extractor", 1, 1, 4, 36),
                new Transfer("SouliumSpawnerContainer", "mysticalagriculture:soulium_spawner", 1, 1, 3, 36),
                new Transfer("OreInfuserContainer", "mysticalagriculture:ore_infuser", 1, 2, 5, 36)
        ), transfers);
    }

    @Test
    void jadeRegistersCompleteProvidersAndProducesNativeMachineSnapshots() {
        var clientTargets = new ArrayList<String>();
        var progressTargets = new ArrayList<String>();
        var itemProviders = new ArrayList<ProviderRegistration<ItemStack>>();
        var energyProviders = new ArrayList<ProviderRegistration<EnergyView.Data>>();
        var progressClients = new ArrayList<IClientExtensionProvider<ProgressView.Data, ProgressView>>();
        var client = proxy(IWailaClientRegistration.class, (method, args) -> {
            if (method.getName().equals("registerBlockComponent")) {
                clientTargets.add(((Class<?>) args[1]).getSimpleName());
            } else if (method.getName().equals("registerProgressClient")) {
                progressClients.add(castProgressClient(args[0]));
            }
            return null;
        });
        var common = proxy(IWailaCommonRegistration.class, (method, args) -> {
            switch (method.getName()) {
                case "registerProgress" -> progressTargets.add(((Class<?>) args[1]).getSimpleName());
                case "registerItemStorage" -> itemProviders.add(new ProviderRegistration<>(
                        castProvider(args[0]),
                        castBlockEntityType(args[1])
                ));
                case "registerEnergyStorage" -> energyProviders.add(new ProviderRegistration<>(
                        castProvider(args[0]),
                        castBlockEntityType(args[1])
                ));
            }
            return null;
        });
        var plugin = new JadeCompat();

        plugin.register(common);
        plugin.registerClient(client);

        assertEquals(List.of(
                "MysticalCropBlock",
                "InferiumCropBlock",
                "InfusedFarmlandBlock"
        ), clientTargets);
        assertEquals(List.of(
                "EssenceFurnaceTileEntity",
                "HarvesterTileEntity",
                "ReprocessorTileEntity",
                "SoulExtractorTileEntity",
                "SouliumSpawnerTileEntity",
                "OreInfuserTileEntity"
        ), progressTargets);
        assertEquals(11, itemProviders.size());
        assertEquals(6, energyProviders.size());
        assertEquals(1, progressClients.size());

        var progressClient = progressClients.getFirst();
        assertEquals("mysticalagriculture:machine_progress", progressClient.getUid().toString());

        var furnace = new EssenceFurnaceTileEntity(BlockPos.ZERO, ModBlocks.FURNACE.defaultBlockState());
        furnace.getInventory().set(0, ItemVariant.of(Items.RAW_IRON), 9);
        furnace.getEnergy().set(12_345);
        var accessor = accessor(furnace);

        var itemProvider = findProvider(itemProviders, EssenceFurnaceTileEntity.class);
        var energyProvider = findProvider(energyProviders, EssenceFurnaceTileEntity.class);
        var items = flatten(itemProvider.getGroups(accessor));
        var energy = flatten(energyProvider.getGroups(accessor));

        assertEquals(9, items.getFirst().getCount());
        assertEquals(List.of(new EnergyView.Data(12_345, 80_000)), energy);
        items.getFirst().setCount(1);
        furnace.getEnergy().set(1);
        assertEquals(9L, furnace.getInventory().getAmountAsLong(0));
        assertEquals(12_345, energy.getFirst().current());
    }

    private static List<IRecipeCategory<?>> categories() {
        var categories = new ArrayList<IRecipeCategory<?>>();
        var helpers = proxy(IJeiHelpers.class, (method, _) ->
                method.getName().equals("getGuiHelper") ? guiHelper() : null
        );
        new JeiCompat().registerCategories(proxy(IRecipeCategoryRegistration.class, (method, args) -> {
            if (method.getName().equals("getJeiHelpers"))
                return helpers;
            if (method.getName().equals("addRecipeCategories"))
                Collections.addAll(categories, (IRecipeCategory<?>[]) args[0]);
            return null;
        }));
        return categories;
    }

    private static IGuiHelper guiHelper() {
        var drawable = proxy(IDrawableStatic.class, (method, args) -> switch (method.getName()) {
            case "getWidth" -> args.length >= 3 ? args[2] : 16;
            case "getHeight" -> args.length >= 4 ? args[3] : 16;
            default -> null;
        });
        var animated = proxy(IDrawableAnimated.class, (_, _) -> null);
        return proxy(IGuiHelper.class, (method, _) -> switch (method.getName()) {
            case "createAnimatedDrawable", "createAnimatedRecipeArrow", "createAnimatedRecipeFlame" -> animated;
            case "createDrawable", "createDrawableIngredient", "createBlankDrawable",
                 "getSlotDrawable", "getOutputSlot", "getRecipeArrow", "getRecipeArrowFilled",
                 "getRecipePlusSign", "getRecipeFlameFilled", "getRecipeFlameEmpty" -> drawable;
            default -> null;
        });
    }

    private static void assertLayout(
            List<IRecipeCategory<?>> categories,
            String categoryName,
            Object recipe,
            List<Slot> expected
    ) {
        var category = categories.stream()
                .filter(candidate -> candidate.getClass().getSimpleName().equals(categoryName))
                .findFirst()
                .orElseThrow();
        var actual = new ArrayList<Slot>();

        castCategory(category).setRecipe(layout(actual), recipe, null);

        assertEquals(expected, actual, categoryName);
    }

    private static IRecipeLayoutBuilder layout(List<Slot> slots) {
        return proxy(IRecipeLayoutBuilder.class, (method, args) -> {
            if (method.getName().equals("addSlot") && args.length == 3) {
                slots.add(new Slot((RecipeIngredientRole) args[0], (int) args[1], (int) args[2]));
                return slotBuilder();
            }
            return null;
        });
    }

    private static IRecipeSlotBuilder slotBuilder() {
        var reference = new IRecipeSlotBuilder[1];
        reference[0] = proxy(IRecipeSlotBuilder.class, (method, _) ->
                method.getReturnType().isAssignableFrom(IRecipeSlotBuilder.class) ? reference[0] : null
        );
        return reference[0];
    }

    private static RecipeHolder<IInfusionRecipe> infusionRecipe() {
        var recipe = proxy(IInfusionRecipe.class, (method, _) -> switch (method.getName()) {
            case "getAltarIngredient" -> Ingredient.of(Items.DIAMOND);
            case "getPedestalIngredients" -> Collections.nCopies(8, Ingredient.of(Items.EMERALD));
            case "assemble" -> new ItemStack(Items.NETHER_STAR);
            default -> null;
        });
        return holder(recipe);
    }

    private static RecipeHolder<IAwakeningRecipe> awakeningRecipe() {
        var recipe = proxy(IAwakeningRecipe.class, (method, _) -> switch (method.getName()) {
            case "getAltarIngredient" -> Ingredient.of(Items.DIAMOND);
            case "getPedestalIngredients" -> Collections.nCopies(4, Ingredient.of(Items.EMERALD));
            case "getEssenceIngredients" -> Collections.nCopies(
                    4,
                    new IngredientWithCount(Ingredient.of(Items.REDSTONE), 40)
            );
            case "assemble" -> new ItemStack(Items.NETHER_STAR);
            default -> null;
        });
        return holder(recipe);
    }

    private static RecipeHolder<IEnchanterRecipe> enchanterRecipe() {
        var supported = HolderSet.direct(Items.BOOK.builtInRegistryHolder());
        var definition = Enchantment.definition(
                supported,
                1,
                2,
                Enchantment.constantCost(1),
                Enchantment.constantCost(10),
                1,
                EquipmentSlotGroup.ANY
        );
        var enchantment = Holder.direct(new Enchantment(
                Component.literal("Task 7"),
                definition,
                HolderSet.direct(),
                DataComponentMap.EMPTY
        ));
        var recipe = proxy(IEnchanterRecipe.class, (method, _) -> switch (method.getName()) {
            case "getIngredients" -> List.of(
                    new IngredientWithCount(Ingredient.of(Items.DIAMOND), 2),
                    new IngredientWithCount(Ingredient.of(Items.LAPIS_LAZULI), 3)
            );
            case "getEnchantment" -> enchantment;
            default -> null;
        });
        return holder(recipe);
    }

    private static RecipeHolder<IReprocessorRecipe> reprocessorRecipe() {
        return holder(proxy(IReprocessorRecipe.class, (method, _) -> switch (method.getName()) {
            case "getIngredient" -> Ingredient.of(Items.WHEAT_SEEDS);
            case "assemble" -> new ItemStack(Items.WHEAT);
            default -> null;
        }));
    }

    private static RecipeHolder<ISoulExtractionRecipe> soulExtractorRecipe() {
        return holder(proxy(ISoulExtractionRecipe.class, (method, _) -> switch (method.getName()) {
            case "getIngredient" -> Ingredient.of(Items.ROTTEN_FLESH);
            case "assemble" -> new ItemStack(ModItems.SOUL_JAR);
            default -> null;
        }));
    }

    private static RecipeHolder<ISouliumSpawnerRecipe> souliumSpawnerRecipe() {
        return holder(proxy(ISouliumSpawnerRecipe.class, (method, _) -> switch (method.getName()) {
            case "getIngredient" -> new IngredientWithCount(Ingredient.of(Items.ROTTEN_FLESH), 16);
            case "getEntityTypes" -> WeightedList.of(EntityTypes.ZOMBIE);
            default -> null;
        }));
    }

    private static RecipeHolder<IOreInfusionRecipe> oreInfusionRecipe() {
        return holder(proxy(IOreInfusionRecipe.class, (method, _) -> switch (method.getName()) {
            case "getIngredients" -> List.of(
                    new IngredientWithCount(Ingredient.of(Items.RAW_IRON), 1),
                    new IngredientWithCount(Ingredient.of(Items.REDSTONE), 4)
            );
            case "assemble" -> new ItemStack(Items.IRON_INGOT);
            default -> null;
        }));
    }

    private static <T extends Recipe<?>> RecipeHolder<T> holder(T recipe) {
        return new RecipeHolder<>(null, recipe);
    }

    private static Slot input(int x, int y) {
        return new Slot(RecipeIngredientRole.INPUT, x, y);
    }

    private static Slot output(int x, int y) {
        return new Slot(RecipeIngredientRole.OUTPUT, x, y);
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static BlockAccessor accessor(BlockEntity blockEntity) {
        return proxy(BlockAccessor.class, (method, _) -> switch (method.getName()) {
            case "getBlockEntity", "getTarget" -> blockEntity;
            case "getBlockState" -> blockEntity.getBlockState();
            case "getBlock" -> blockEntity.getBlockState().getBlock();
            case "getPosition" -> blockEntity.getBlockPos();
            case "getAccessorType" -> BlockAccessor.class;
            default -> null;
        });
    }

    private static <T> IServerExtensionProvider<T> findProvider(
            List<ProviderRegistration<T>> registrations,
            Class<? extends BlockEntity> target
    ) {
        return registrations.stream()
                .filter(registration -> registration.target() == target)
                .findFirst()
                .orElseThrow()
                .provider();
    }

    private static <T> List<T> flatten(List<ViewGroup<T>> groups) {
        return groups.stream().flatMap(group -> group.views.stream()).toList();
    }

    @SuppressWarnings("unchecked")
    private static IRecipeCategory<Object> castCategory(IRecipeCategory<?> category) {
        return (IRecipeCategory<Object>) category;
    }

    @SuppressWarnings("unchecked")
    private static ISubtypeInterpreter<ItemStack> castSubtype(Object subtype) {
        return (ISubtypeInterpreter<ItemStack>) subtype;
    }

    @SuppressWarnings("unchecked")
    private static <T> IServerExtensionProvider<T> castProvider(Object provider) {
        return (IServerExtensionProvider<T>) provider;
    }

    @SuppressWarnings("unchecked")
    private static IClientExtensionProvider<ProgressView.Data, ProgressView> castProgressClient(Object provider) {
        return (IClientExtensionProvider<ProgressView.Data, ProgressView>) provider;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends BlockEntity> castBlockEntityType(Object type) {
        return (Class<? extends BlockEntity>) type;
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (_, method, args) -> {
            var actualArgs = args == null ? new Object[0] : args;
            var result = invocation.accept(method, actualArgs);
            return result != null ? result : defaultValue(method.getReturnType());
        }));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class)
            return null;
        if (!type.isPrimitive())
            return null;
        if (type == boolean.class)
            return false;
        if (type == char.class)
            return '\0';
        if (type == byte.class)
            return (byte) 0;
        if (type == short.class)
            return (short) 0;
        if (type == int.class)
            return 0;
        if (type == long.class)
            return 0L;
        if (type == float.class)
            return 0F;
        if (type == double.class)
            return 0D;
        throw new IllegalArgumentException("Unsupported primitive " + type);
    }

    @FunctionalInterface
    private interface Invocation {
        Object accept(java.lang.reflect.Method method, Object[] args);
    }

    private record Catalyst(String recipeType, String item) {
    }

    private record ClickArea(
            String screen,
            int x,
            int y,
            int width,
            int height,
            String recipeType
    ) {
    }

    private record Slot(RecipeIngredientRole role, int x, int y) {
    }

    private record Transfer(
            String container,
            String recipeType,
            int recipeSlotStart,
            int recipeSlotCount,
            int inventorySlotStart,
            int inventorySlotCount
    ) {
    }

    private record ProviderRegistration<T>(
            IServerExtensionProvider<T> provider,
            Class<? extends BlockEntity> target
    ) {
    }
}
