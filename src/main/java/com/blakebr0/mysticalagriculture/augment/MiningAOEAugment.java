package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.cucumber.helper.BlockHelper;
import com.blakebr0.cucumber.helper.ColorHelper;
import com.blakebr0.mysticalagriculture.api.tinkering.AOEAugment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class MiningAOEAugment extends AOEAugment {
    private static final ThreadLocal<Boolean> HARVESTING_AOE =
            ThreadLocal.withInitial(() -> false);

    public MiningAOEAugment(Identifier id, int tier, int range) {
        super(id, tier, EnumSet.of(AugmentType.PICKAXE, AugmentType.AXE, AugmentType.SHOVEL), getColor(0xD5FFF6, tier), getColor(0x0EBABD, tier), range);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (level.isClientSide())
            return false;

        if (entity.isShiftKeyDown())
            return false;

        if (HARVESTING_AOE.get())
            return false;

        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            var trace = BlockHelper.rayTraceBlocks(level, player);
            var side = trace.getDirection();

            HARVESTING_AOE.set(true);
            try {
                harvestAOEBlocks(stack, this.range, serverLevel, state, pos, side, player);
            } finally {
                HARVESTING_AOE.remove();
            }
        }

        return false;
    }

    private static void harvestAOEBlocks(
            ItemStack stack,
            int radius,
            ServerLevel level,
            BlockState state,
            BlockPos pos,
            Direction side,
            ServerPlayer player
    ) {
        var hardness = state.getDestroySpeed(level, pos);

        if (radius > 0 && hardness >= 0.2F && canHarvestBlock(stack, state)) {
            getAOEBlocks(stack, radius, pos, side, player).forEach(aoePos -> {
                if (!aoePos.equals(pos)
                        && level.mayInteract(player, aoePos)
                        && player.mayUseItemAt(aoePos.relative(side), side, stack)) {
                    var aoeState = level.getBlockState(aoePos);

                    if (canHarvestBlock(stack, aoeState) && !aoeState.hasBlockEntity() && aoeState.getDestroySpeed(level, aoePos) <= hardness + 5.0F) {
                        BlockHelper.harvestAOEBlock(stack, level, player, aoePos.immutable());
                    }
                }
            });
        }
    }

    private static boolean canHarvestBlock(ItemStack stack, BlockState state) {
        return stack.isCorrectToolForDrops(state) || (!state.requiresCorrectToolForDrops() && stack.getDestroySpeed(state) > 1.0F);
    }

    private static int getColor(int color, int tier) {
        return ColorHelper.saturate(color, Math.min((float) tier / 5, 1));
    }
}
