package com.blakebr0.mysticalagriculture.item;

import com.blakebr0.cucumber.item.BaseItem;
import com.blakebr0.mysticalagriculture.api.crop.ICropProvider;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SaplingBlock;

import java.util.function.Consumer;

public class MysticalFertilizerItem extends BaseItem {
    public MysticalFertilizerItem(Identifier id) {
        super(id);

        DispenserBlock.registerBehavior(this, new DispenserBehavior());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var level = context.getLevel();

        if (applyFertilizer(stack, level, pos, player)) {
            if (!level.isClientSide()) {
                level.levelEvent(1505, pos, 0);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(ModTooltips.MYSTICAL_FERTILIZER.toComponent());
    }

    public static boolean applyFertilizer(ItemStack stack, Level level, BlockPos pos, Player player) {
        var state = level.getBlockState(pos);

        var block = state.getBlock();

        if (block instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, pos, state)) {
            if (!level.isClientSide()) {
                var rand = level.getRandom();

                if (growable.isBonemealSuccess(level, rand, pos, state) || canGrowResourceCrops(growable) || growable instanceof SaplingBlock) {
                    var serverWorld = (ServerLevel) level;

                    if (growable instanceof CropBlock crop) {
                        level.setBlock(pos, crop.getStateForAge(crop.getMaxAge()), 2);
                    } else if (growable instanceof SaplingBlock sapling) {
                        var chunkGenerator = serverWorld.getChunkSource().getGenerator();

                        sapling.treeGrower.growTree(serverWorld, chunkGenerator, pos, state, rand);
                    } else {
                        growable.performBonemeal(serverWorld, rand, pos, state);
                    }
                }

                stack.shrink(1);
            }

            return true;
        }

        return false;
    }

    private static boolean canGrowResourceCrops(BonemealableBlock growable) {
        return growable instanceof ICropProvider cropGetter && cropGetter.getCrop().getTier().isFertilizable();
    }

    public static class DispenserBehavior extends OptionalDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            this.setSuccess(true);

            var level = source.level();
            var pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));

            if (MysticalFertilizerItem.applyFertilizer(stack, level, pos, null)) {
                if (!level.isClientSide()) {
                    level.levelEvent(2005, pos, 0);
                }
            } else {
                this.setSuccess(false);
            }

            return stack;
        }
    }
}
