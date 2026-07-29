package com.blakebr0.mysticalagriculture.compat;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crop.ICropProvider;
import com.blakebr0.mysticalagriculture.api.farmland.IEssenceFarmland;
import com.blakebr0.mysticalagriculture.block.InferiumCropBlock;
import com.blakebr0.mysticalagriculture.block.InfusedFarmlandBlock;
import com.blakebr0.mysticalagriculture.block.MysticalCropBlock;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningPedestalTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceVesselTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.InfusionAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.InfusionPedestalTileEntity;
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
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
    private static final Identifier CROP_PROVIDER = MysticalAgriculture.resource("crop");
    private static final Identifier INFERIUM_CROP_PROVIDER = MysticalAgriculture.resource("inferium_crop");
    private static final Identifier INFUSED_FARMLAND_PROVIDER = MysticalAgriculture.resource("infused_farmland");
    private static final Identifier MACHINE_PROGRESS_PROVIDER = MysticalAgriculture.resource("machine_progress");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerItemStorage(items(
                AwakeningAltarTileEntity.class,
                AwakeningAltarTileEntity::getInventory
        ), AwakeningAltarTileEntity.class);
        registration.registerItemStorage(items(
                AwakeningPedestalTileEntity.class,
                AwakeningPedestalTileEntity::getInventory
        ), AwakeningPedestalTileEntity.class);
        registration.registerItemStorage(items(
                EssenceVesselTileEntity.class,
                EssenceVesselTileEntity::getInventory
        ), EssenceVesselTileEntity.class);
        registration.registerItemStorage(items(
                EssenceFurnaceTileEntity.class,
                EssenceFurnaceTileEntity::getInventory
        ), EssenceFurnaceTileEntity.class);
        registration.registerItemStorage(items(
                HarvesterTileEntity.class,
                HarvesterTileEntity::getInventory
        ), HarvesterTileEntity.class);
        registration.registerItemStorage(items(
                InfusionAltarTileEntity.class,
                InfusionAltarTileEntity::getInventory
        ), InfusionAltarTileEntity.class);
        registration.registerItemStorage(items(
                InfusionPedestalTileEntity.class,
                InfusionPedestalTileEntity::getInventory
        ), InfusionPedestalTileEntity.class);
        registration.registerItemStorage(items(
                ReprocessorTileEntity.class,
                ReprocessorTileEntity::getInventory
        ), ReprocessorTileEntity.class);
        registration.registerItemStorage(items(
                SoulExtractorTileEntity.class,
                SoulExtractorTileEntity::getInventory
        ), SoulExtractorTileEntity.class);
        registration.registerItemStorage(items(
                SouliumSpawnerTileEntity.class,
                SouliumSpawnerTileEntity::getInventory
        ), SouliumSpawnerTileEntity.class);
        registration.registerItemStorage(items(
                OreInfuserTileEntity.class,
                OreInfuserTileEntity::getInventory
        ), OreInfuserTileEntity.class);

        registration.registerEnergyStorage(energy(
                EssenceFurnaceTileEntity.class,
                EssenceFurnaceTileEntity::getEnergy
        ), EssenceFurnaceTileEntity.class);
        registration.registerEnergyStorage(energy(
                HarvesterTileEntity.class,
                HarvesterTileEntity::getEnergy
        ), HarvesterTileEntity.class);
        registration.registerEnergyStorage(energy(
                ReprocessorTileEntity.class,
                ReprocessorTileEntity::getEnergy
        ), ReprocessorTileEntity.class);
        registration.registerEnergyStorage(energy(
                SoulExtractorTileEntity.class,
                SoulExtractorTileEntity::getEnergy
        ), SoulExtractorTileEntity.class);
        registration.registerEnergyStorage(energy(
                SouliumSpawnerTileEntity.class,
                SouliumSpawnerTileEntity::getEnergy
        ), SouliumSpawnerTileEntity.class);
        registration.registerEnergyStorage(energy(
                OreInfuserTileEntity.class,
                OreInfuserTileEntity::getEnergy
        ), OreInfuserTileEntity.class);

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
                "Registered Jade item snapshots for 11 storage owners, energy snapshots for 6 machines, and progress for 6 machines"
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerProgressClient(new IClientExtensionProvider<>() {
            @Override
            public List<ClientViewGroup<ProgressView>> getClientGroups(
                    Accessor<?> accessor,
                    List<ViewGroup<ProgressView.Data>> groups
            ) {
                return ClientViewGroup.map(groups, ProgressView::read, null);
            }

            @Override
            public Identifier getUid() {
                return MACHINE_PROGRESS_PROVIDER;
            }
        });

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
        MysticalAgriculture.LOGGER.info(
                "Registered 3 Mystical Agriculture Jade block tooltip providers and the machine progress renderer"
        );
    }

    private static <T extends BlockEntity> IServerExtensionProvider<ItemStack> items(
            Class<T> type,
            Function<T, CItemStacksHandler> inventory
    ) {
        return new IServerExtensionProvider<>() {
            @Override
            public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
                if (!(accessor instanceof BlockAccessor blockAccessor))
                    return List.of();

                var blockEntity = blockAccessor.getBlockEntity();
                if (!type.isInstance(blockEntity))
                    return List.of();

                var handler = inventory.apply(type.cast(blockEntity));
                var snapshots = new ArrayList<ItemStack>();
                for (int slot = 0; slot < handler.getContainerSize(); slot++) {
                    var resource = handler.getResource(slot);
                    var amount = handler.getAmountAsInt(slot);
                    if (!resource.isBlank() && amount > 0)
                        snapshots.add(resource.toStack(amount));
                }

                if (snapshots.isEmpty())
                    return List.of();

                return List.of(new ViewGroup<>(List.copyOf(snapshots)));
            }

            @Override
            public Identifier getUid() {
                return JadeIds.UNIVERSAL_ITEM_STORAGE_DEFAULT;
            }

            @Override
            public int getDefaultPriority() {
                return 9999;
            }
        };
    }

    private static <T extends BlockEntity> IServerExtensionProvider<EnergyView.Data> energy(
            Class<T> type,
            Function<T, EnergyStorage> energy
    ) {
        return new IServerExtensionProvider<>() {
            @Override
            public List<ViewGroup<EnergyView.Data>> getGroups(Accessor<?> accessor) {
                if (!(accessor instanceof BlockAccessor blockAccessor))
                    return List.of();

                var blockEntity = blockAccessor.getBlockEntity();
                if (!type.isInstance(blockEntity))
                    return List.of();

                var storage = energy.apply(type.cast(blockEntity));
                var capacity = storage.getCapacity();
                if (capacity <= 0)
                    return List.of();

                var current = Math.clamp(storage.getAmount(), 0, capacity);
                return List.of(new ViewGroup<>(List.of(
                        new EnergyView.Data(current, capacity)
                )));
            }

            @Override
            public Identifier getUid() {
                return JadeIds.UNIVERSAL_ENERGY_STORAGE_DEFAULT;
            }

            @Override
            public int getDefaultPriority() {
                return 9999;
            }
        };
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
