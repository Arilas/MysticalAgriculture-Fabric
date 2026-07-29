package com.blakebr0.mysticalagriculture.block;

import com.blakebr0.cucumber.block.BaseTileEntityBlock;
import com.blakebr0.cucumber.helper.BlockHelper;
import com.blakebr0.cucumber.util.VoxelShapeBuilder;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningPedestalTileEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AwakeningPedestalBlock extends BaseTileEntityBlock {
    public static final VoxelShape PEDESTAL_SHAPE = VoxelShapeBuilder.fromShapes(
            Shapes.box(0.125, 0, 0.125, 0.3125, 0.125, 0.3125),
            Shapes.box(0.6875, 0, 0.125, 0.875, 0.125, 0.3125),
            Shapes.box(0.125, 0, 0.6875, 0.3125, 0.125, 0.875),
            Shapes.box(0.6875, 0, 0.6875, 0.875, 0.125, 0.875),
            Shapes.box(0.1875, 0.125, 0.1875, 0.8125, 0.25, 0.8125),
            Shapes.box(0.25, 0.25, 0.25, 0.75, 0.875, 0.75),
            Shapes.box(0.1875, 0.875, 0.1875, 0.8125, 1, 0.3125),
            Shapes.box(0.1875, 0.875, 0.6875, 0.8125, 1, 0.8125),
            Shapes.box(0.1875, 0.875, 0.3125, 0.3125, 1, 0.6875),
            Shapes.box(0.6875, 0.875, 0.3125, 0.8125, 1, 0.6875),
            Shapes.box(0.8125, 0.6875, 0.3125, 0.8125, 0.875, 0.6875),
            Shapes.box(0.1875, 0.6875, 0.3125, 0.1875, 0.875, 0.6875),
            Shapes.box(0.3125, 0.6875, 0.1875, 0.6875, 0.875, 0.1875),
            Shapes.box(0.3125, 0.6875, 0.8125, 0.6875, 0.875, 0.8125)
    ).build();

    public AwakeningPedestalBlock(Identifier id) {
        super(id, SoundType.STONE, 10.0F, 12.0F, true);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AwakeningPedestalTileEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PEDESTAL_SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var tile = level.getBlockEntity(pos);

        if (tile instanceof AwakeningPedestalTileEntity pedestal) {
            var inventory = pedestal.getInventory();
            var input = inventory.getResource(0);
            var held = player.getItemInHand(hand);

            if (input.isBlank() && !held.isEmpty()) {
                inventory.set(0, ItemVariant.of(held), 1);
                player.setItemInHand(hand, held.copyWithCount(held.count() - 1));
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else if (!input.isBlank()) {
                var amount = inventory.getAmountAsInt(0);
                var item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), input.toStack(amount));

                inventory.set(0, ItemVariant.blank(), 0);

                item.setNoPickUpDelay();
                level.addFreshEntity(item);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return BlockHelper.getRedstoneSignalFromInventory(level.getBlockEntity(pos));
    }
}
