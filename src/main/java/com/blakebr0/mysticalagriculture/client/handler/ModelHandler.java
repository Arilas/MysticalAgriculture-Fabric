package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.cucumber.event.RegisterClientItemsEvent;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropModels;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModelHandler {
    private static final Map<CropType, List<ModelReference>> STEM_MODELS = new IdentityHashMap<>();
    private static final Map<Identifier, ModelReference> FLOWER_MODELS = new HashMap<>();
    private static final Map<Block, Crop> CROPS_BY_BLOCK = new IdentityHashMap<>();
    private static boolean registered;

    private ModelHandler() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        prepareReferences();
        ModelLoadingPlugin.register(ModelHandler::initialize);
        registered = true;
    }

    private static void prepareReferences() {
        STEM_MODELS.clear();
        FLOWER_MODELS.clear();
        CROPS_BY_BLOCK.clear();

        addStemModels(CropType.RESOURCE, "block/mystical_resource_crop_");
        addStemModels(CropType.MOB, "block/mystical_mob_crop_");

        for (var type : CropRegistry.getInstance().getTypes()) {
            addFlowerModel(type, CropModels.FLOWER_INGOT_BLANK);
            addFlowerModel(type, CropModels.FLOWER_ROCK_BLANK);
            addFlowerModel(type, CropModels.FLOWER_DUST_BLANK);
            addFlowerModel(type, CropModels.FLOWER_FACE_BLANK);
        }

        for (var crop : CropRegistry.getInstance().getCrops()) {
            if (crop.getCropBlock() != null) {
                CROPS_BY_BLOCK.put(crop.getCropBlock(), crop);
            }
        }
    }

    private static void initialize(ModelLoadingPlugin.Context context) {
        STEM_MODELS.values().stream()
                .flatMap(List::stream)
                .forEach(reference -> context.addModel(
                        reference.key(),
                        SimpleUnbakedExtraModel.blockStateModel(reference.id())
                ));
        FLOWER_MODELS.values().forEach(reference -> context.addModel(
                reference.key(),
                SimpleUnbakedExtraModel.blockStateModel(reference.id())
        ));
        context.modifyBlockModelAfterBake().register(
                ModelModifier.OVERRIDE_PHASE,
                ModelHandler::replaceMissingCropModel
        );
    }

    private static BlockStateModel replaceMissingCropModel(
            BlockStateModel model,
            ModelModifier.AfterBakeBlock.Context context
    ) {
        var crop = CROPS_BY_BLOCK.get(context.state().getBlock());
        if (crop == null || !isMissing(model, context))
            return model;

        int age = crop.getCropBlock().getAge(context.state());
        Identifier replacement;
        if (age < 7) {
            var stems = STEM_MODELS.get(crop.getType());
            if (stems == null || age >= stems.size())
                return model;
            replacement = stems.get(age).id();
        } else {
            var flower = crop.getModels().getFlowerModel().withSuffix("_" + crop.getType().getName());
            var reference = FLOWER_MODELS.get(flower);
            if (reference == null)
                return model;
            replacement = reference.id();
        }

        return new SingleVariant.Unbaked(new Variant(replacement)).bake(context.baker());
    }

    private static boolean isMissing(
            BlockStateModel model,
            ModelModifier.AfterBakeBlock.Context context
    ) {
        var parts = new ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
        model.collectParts(RandomSource.create(0), parts);
        return parts.size() == 1 && parts.getFirst() == context.baker().missingBlockModelPart();
    }

    public static void onRegisterClientItems(RegisterClientItemsEvent event) {
        for (var crop : CropRegistry.getInstance().getCrops()) {
            var models = crop.getModels();

            var essence = crop.getEssenceItem();
            var essenceId = BuiltInRegistries.ITEM.getKey(essence);

            if (!event.hasClientItem(essenceId)) {
                var texture = models.getEssenceModel();
                List<ItemTintSource> tints = crop.isEssenceColored()
                        ? List.of(new Constant(crop.getEssenceColor()))
                        : List.of();

                event.register(essenceId, new ClientItem(
                        new CuboidItemModelWrapper.Unbaked(texture, Optional.empty(), tints),
                        ClientItem.Properties.DEFAULT
                ));
            }

            var seeds = crop.getSeedsItem();
            var seedsId = BuiltInRegistries.ITEM.getKey(seeds);

            if (!event.hasClientItem(seedsId)) {
                var texture = models.getSeedModel();
                List<ItemTintSource> tints = crop.isSeedColored()
                        ? List.of(new Constant(crop.getSeedColor()))
                        : List.of();

                event.register(seedsId, new ClientItem(
                        new CuboidItemModelWrapper.Unbaked(texture, Optional.empty(), tints),
                        ClientItem.Properties.DEFAULT
                ));
            }
        }
    }

    private static void addStemModels(CropType type, String prefix) {
        var references = new ArrayList<ModelReference>();
        for (int age = 0; age < 8; age++) {
            references.add(reference(MysticalAgriculture.resource(prefix + age)));
        }
        STEM_MODELS.put(type, List.copyOf(references));
    }

    private static void addFlowerModel(CropType type, Identifier base) {
        var id = base.withSuffix("_" + type.getName());
        FLOWER_MODELS.put(id, reference(id));
    }

    private static ModelReference reference(Identifier id) {
        return new ModelReference(id, ExtraModelKey.create(id::toString));
    }

    private record ModelReference(Identifier id, ExtraModelKey<BlockStateModel> key) {
    }
}
