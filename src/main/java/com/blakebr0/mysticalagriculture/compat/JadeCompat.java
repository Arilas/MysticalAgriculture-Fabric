package com.blakebr0.mysticalagriculture.compat;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crop.ICropProvider;
import com.blakebr0.mysticalagriculture.api.farmland.IEssenceFarmland;
import com.blakebr0.mysticalagriculture.block.InferiumCropBlock;
import com.blakebr0.mysticalagriculture.block.InfusedFarmlandBlock;
import com.blakebr0.mysticalagriculture.block.MysticalCropBlock;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

import java.util.List;
import java.util.function.ToIntFunction;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
    private static final Identifier CROP_PROVIDER = MysticalAgriculture.resource("crop");
    private static final Identifier INFERIUM_CROP_PROVIDER = MysticalAgriculture.resource("inferium_crop");
    private static final Identifier INFUSED_FARMLAND_PROVIDER = MysticalAgriculture.resource("infused_farmland");
    private static final Identifier MACHINE_PROGRESS_PROVIDER = MysticalAgriculture.resource("machine_progress");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerProgress(progress(
                EssenceFurnaceTileEntity.class,
                EssenceFurnaceTileEntity::getProgress,
                EssenceFurnaceTileEntity::getOperationTime
        ), EssenceFurnaceTileEntity.class);
        registration.registerProgress(progress(
                HarvesterTileEntity.class,
                HarvesterTileEntity::getProgress,
                HarvesterTileEntity::getOperationTime
        ), HarvesterTileEntity.class);
        registration.registerProgress(progress(
                ReprocessorTileEntity.class,
                ReprocessorTileEntity::getProgress,
                ReprocessorTileEntity::getOperationTime
        ), ReprocessorTileEntity.class);
        registration.registerProgress(progress(
                SoulExtractorTileEntity.class,
                SoulExtractorTileEntity::getProgress,
                SoulExtractorTileEntity::getOperationTime
        ), SoulExtractorTileEntity.class);
        registration.registerProgress(progress(
                SouliumSpawnerTileEntity.class,
                SouliumSpawnerTileEntity::getProgress,
                SouliumSpawnerTileEntity::getOperationTime
        ), SouliumSpawnerTileEntity.class);
        registration.registerProgress(progress(
                OreInfuserTileEntity.class,
                OreInfuserTileEntity::getProgress,
                OreInfuserTileEntity::getOperationTime
        ), OreInfuserTileEntity.class);
        MysticalAgriculture.LOGGER.info(
                "Registered Jade progress providers for 6 machines; Jade universal storage supplies item and energy views"
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                var block = accessor.getBlock();
                var crop = ((ICropProvider) block).getCrop();

                tooltip.add(ModTooltips.TIER.args(crop.getTier().getDisplayName()).toComponent());

                var pos = accessor.getPosition();
                var downPos = pos.below();
                var level = accessor.getLevel();
                var belowBlock = level.getBlockState(downPos).getBlock();

                if (ModConfigs.REQUIRES_EFFECTIVE_FARMLAND.get() && crop != ModCrops.INFERIUM) {
                    var farmland = crop.getTier().getFarmlandBlock();
                    if (farmland != null) {
                        tooltip.add(ModTooltips.REQUIRES_EFFECTIVE_FARMLAND.args(farmland.getName().withStyle(crop.getTier().getTextColor())).toComponent());
                    }
                }

                if (ModConfigs.SECONDARY_SEED_DROPS.get()) {
                    var secondaryChance = crop.getSecondaryChance(belowBlock);
                    if (secondaryChance > 0) {
                        var chanceText = Component.literal(String.valueOf((int) (secondaryChance * 100)))
                                .append("%")
                                .withStyle(crop.getTier().getTextColor());

                        tooltip.add(ModTooltips.SECONDARY_CHANCE.args(chanceText).toComponent());
                    }
                }

                var crux = crop.getCruxBlock();
                if (crux != null) {
                    var stack = new ItemStack(crux);
                    tooltip.add(ModTooltips.REQUIRES_CRUX.args(stack.getHoverName()).toComponent());
                }

                var biomes = crop.getRequiredBiomes();
                if (!biomes.isEmpty()) {
                    var biome = level.getBiome(pos).unwrapKey();
                    if (biome.isPresent() && !biomes.contains(biome.get().identifier())) {
                        tooltip.add(ModTooltips.INVALID_BIOME.color(ChatFormatting.RED).toComponent());
                    }
                }
            }

            @Override
            public Identifier getUid() {
                return CROP_PROVIDER;
            }
        }, MysticalCropBlock.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                var block = accessor.getBlock();
                var crop = ((ICropProvider) block).getCrop();
                var downPos = accessor.getPosition().below();
                var belowBlock = accessor.getLevel().getBlockState(downPos).getBlock();

                int output = 100;
                if (belowBlock instanceof IEssenceFarmland farmland) {
                    int tier = farmland.getTier().getValue();
                    output = (tier * 50) + 50;
                }

                var inferiumOutputText = Component.literal(String.valueOf(output)).append("%").withStyle(crop.getTier().getTextColor());

                tooltip.add(ModTooltips.INFERIUM_OUTPUT.args(inferiumOutputText).toComponent());
            }

            @Override
            public Identifier getUid() {
                return INFERIUM_CROP_PROVIDER;
            }
        }, InferiumCropBlock.class);

        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                var block = accessor.getBlock();
                var farmland = (IEssenceFarmland) block;

                tooltip.add(ModTooltips.TIER.args(farmland.getTier().getDisplayName()).toComponent());
            }

            @Override
            public Identifier getUid() {
                return INFUSED_FARMLAND_PROVIDER;
            }
        }, InfusedFarmlandBlock.class);
        MysticalAgriculture.LOGGER.info("Registered 3 Mystical Agriculture Jade block tooltip providers");
    }

    private static <T extends BlockEntity> IServerExtensionProvider<ProgressView.Data> progress(
            Class<T> type,
            ToIntFunction<T> progress,
            ToIntFunction<T> target
    ) {
        return new IServerExtensionProvider<>() {
            @Override
            public List<ViewGroup<ProgressView.Data>> getGroups(Accessor<?> accessor) {
                if (!(accessor instanceof BlockAccessor blockAccessor))
                    return List.of();

                var blockEntity = blockAccessor.getBlockEntity();
                if (!type.isInstance(blockEntity))
                    return List.of();

                var machine = type.cast(blockEntity);
                var current = progress.applyAsInt(machine);
                var maximum = target.applyAsInt(machine);
                if (current <= 0 || maximum <= 0)
                    return List.of();

                return List.of(new ViewGroup<>(List.of(
                        new ProgressView.Data(current, 0, maximum)
                )));
            }

            @Override
            public Identifier getUid() {
                return MACHINE_PROGRESS_PROVIDER;
            }
        };
    }
}
