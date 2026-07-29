package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.cucumber.helper.ColorHelper;
import com.blakebr0.mysticalagriculture.api.tinkering.AOEAugment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.EnumSet;

public class TillingAOEAugment extends AOEAugment {
    public TillingAOEAugment(Identifier id, int tier, int range) {
        super(id, tier, EnumSet.of(AugmentType.HOE), getColor(0xB9855C, tier), getColor(0x593D29, tier), range);
    }

    @Override
    public boolean onItemUse(UseOnContext context) {
        var player = context.getPlayer();

        if (player == null)
            return false;

        var level = context.getLevel();
        var pos = context.getClickedPos();

        var playedSound = false;

        if (till(context, pos)) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            playedSound = true;

            if (!player.isCrouching())
                return false;
        }

        if (player.isCrouching()) {
            var positions = getAOEBlocks(player.getMainHandItem(), this.range, pos, Direction.UP, player).iterator();

            while (positions.hasNext()) {
                var aoePos = positions.next();

                if (till(context, aoePos) && !playedSound) {
                    level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                    playedSound = true;
                }
            }
        }

        return true;
    }

    private static boolean till(UseOnContext context, BlockPos pos) {
        var level = context.getLevel();
        var direction = context.getClickedFace();
        var player = context.getPlayer();
        var stack = context.getItemInHand();

        if (direction != Direction.DOWN
                && level.isEmptyBlock(pos.above())
                && player != null
                && level.mayInteract(player, pos)
                && player.mayUseItemAt(pos.relative(direction), direction, stack)) {
            var state = level.getBlockState(pos);
            var modifiedState = getTilledState(state);
            if (modifiedState != null) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, modifiedState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, modifiedState));

                    if (state.is(Blocks.ROOTED_DIRT)) {
                        Block.popResourceFromFace(level, pos, direction, new net.minecraft.world.item.ItemStack(Items.HANGING_ROOTS));
                    }

                    stack.hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
                }

                return true;
            }
        }

        return false;
    }

    private static BlockState getTilledState(BlockState state) {
        var block = state.getBlock();
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT) {
            return Blocks.FARMLAND.defaultBlockState();
        }
        if (block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
            return Blocks.DIRT.defaultBlockState();
        }
        return null;
    }

    private static int getColor(int color, int tier) {
        return ColorHelper.saturate(color, Math.min((float) tier / 5, 1));
    }
}
