package com.blakebr0.mysticalagriculture.block;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.ICropProvider;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class MysticalCropBlock extends CropBlock implements ICropProvider {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private final Crop crop;

    public MysticalCropBlock(Identifier id, Crop crop) {
        super(Properties.ofFullCopy(Blocks.WHEAT).setId(ResourceKey.create(Registries.BLOCK, id)));
        this.crop = crop;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canGrow(level, pos))
            return;

        super.randomTick(state, level, pos, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int age = state.getValue(AGE);

        int crop = 0;
        int seed = 1;
        int fertilizer = 0;

        if (age == this.getMaxAge()) {
            crop = 1;

            var vec = builder.getOptionalParameter(LootContextParams.ORIGIN);

            if (vec != null) {
                var level = builder.getLevel();
                var pos = BlockPos.containing(vec);
                var below = level.getBlockState(pos.below()).getBlock();
                double chance = this.crop.getSecondaryChance(below);

                if (Math.random() < chance)
                    crop = 2;

                if (ModConfigs.SECONDARY_SEED_DROPS.get() && Math.random() < chance)
                    seed = 2;

                double fertilizerChance = ModConfigs.FERTILIZED_ESSENCE_DROP_CHANCE.get();
                if (Math.random() < fertilizerChance)
                    fertilizer = 1;
            }
        }

        List<ItemStack> drops = new ArrayList<>();

        if (crop > 0)
            drops.add(new ItemStack(this.getCropsItem(), crop));

        drops.add(new ItemStack(this.getBaseSeedId(), seed));

        if (fertilizer > 0)
            drops.add(new ItemStack(ModItems.FERTILIZED_ESSENCE));

        return drops;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!this.canGrow(level, pos))
            return;

        super.performBonemeal(level, random, pos, state);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (level instanceof Level) {
            return this.canGrow((Level) level, pos) && super.isValidBonemealTarget(level, pos, state);
        }

        return super.isValidBonemealTarget(level, pos, state);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return this.crop.getSeedsItem();
    }

    @Override
    public Crop getCrop() {
        return this.crop;
    }

    protected ItemLike getCropsItem() {
        return this.crop.getEssenceItem();
    }

    private boolean canGrow(Level level, BlockPos pos) {
        var crux = this.crop.getCruxBlock();

        if (crux != null) {
            var block = level.getBlockState(pos.below(2)).getBlock();
            if (block != crux)
                return false;
        }

        if (ModConfigs.REQUIRES_EFFECTIVE_FARMLAND.get() && this.crop != ModCrops.INFERIUM) {
            var farmland = level.getBlockState(pos.below()).getBlock();
            if (!this.crop.getTier().isEffectiveFarmland(farmland))
                return false;
        }

        var biomes = this.crop.getRequiredBiomes();

        if (!biomes.isEmpty()) {
            var biome = level.getBiome(pos).getKey();
            return biome != null && biomes.contains(biome.identifier());
        }

        return true;
    }
}
