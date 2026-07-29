package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.cucumber.helper.ColorHelper;
import com.blakebr0.mysticalagriculture.api.tinkering.AOEAugment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.EnumSet;

public class PathingAOEAugment extends AOEAugment {
    public PathingAOEAugment(Identifier id, int tier, int range) {
        super(id, tier, EnumSet.of(AugmentType.SHOVEL), getColor(0xAA8D4A, tier), getColor(0x856B3A, tier), range);
    }

    @Override
    public boolean onItemUse(UseOnContext context) {
        var player = context.getPlayer();

        if (player == null)
            return false;

        var level = context.getLevel();
        var pos = context.getClickedPos();

        var playedSound = false;

        if (path(context, pos)) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);

            playedSound = true;

            if (!player.isCrouching())
                return false;
        }

        if (player.isCrouching()) {
            var positions = getAOEBlocks(player.getMainHandItem(), this.range, pos, Direction.UP, player).iterator();

            while (positions.hasNext()) {
                var aoePos = positions.next();

                if (path(context, aoePos) && !playedSound) {
                    level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                    playedSound = true;
                }
            }
        }

        return true;
    }

    private static boolean path(UseOnContext context, BlockPos pos) {
        var level = context.getLevel();
        var direction = context.getClickedFace();
        var player = context.getPlayer();
        var stack = context.getItemInHand();

        if (direction != Direction.DOWN
                && level.isEmptyBlock(pos.above())
                && player != null
                && level.mayInteract(player, pos)
                && player.mayUseItemAt(pos.relative(direction), direction, stack)) {
            var modifiedState = getPathState(level.getBlockState(pos));
            if (modifiedState != null) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, modifiedState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, modifiedState));
                    stack.hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
                }

                return true;
            }
        }

        return false;
    }

    private static BlockState getPathState(BlockState state) {
        var block = state.getBlock();
        if (block == Blocks.GRASS_BLOCK
                || block == Blocks.DIRT
                || block == Blocks.PODZOL
                || block == Blocks.COARSE_DIRT
                || block == Blocks.MYCELIUM
                || block == Blocks.ROOTED_DIRT) {
            return Blocks.DIRT_PATH.defaultBlockState();
        }
        return null;
    }

    private static int getColor(int color, int tier) {
        return ColorHelper.saturate(color, Math.min((float) tier / 5, 1));
    }
}
