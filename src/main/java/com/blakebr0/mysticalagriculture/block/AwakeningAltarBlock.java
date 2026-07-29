package com.blakebr0.mysticalagriculture.block;

import com.blakebr0.cucumber.block.BaseTileEntityBlock;
import com.blakebr0.cucumber.helper.BlockHelper;
import com.blakebr0.cucumber.iface.IHoverTextProvider;
import com.blakebr0.cucumber.util.VoxelShapeBuilder;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.item.WandItem;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningAltarTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.Consumer;

public class AwakeningAltarBlock extends BaseTileEntityBlock implements IHoverTextProvider {
    public static final VoxelShape ALTAR_SHAPE = VoxelShapeBuilder.builder()
            .cuboid(0, 0, 0, 16, 8, 16).cuboid(3, 13, 3, 13, 14, 13)
            .cuboid(6, 10, 6, 10, 11, 10).cuboid(5, 11, 5, 11, 13, 11)
            .cuboid(2, 13.5, 4, 3, 14.5, 12).cuboid(13, 13.5, 4, 14, 14.5, 12)
            .cuboid(4, 13.5, 2, 12, 14.5, 3).cuboid(4, 13.5, 13, 12, 14.5, 14)
            .cuboid(0.5, 8, 4, 4, 10, 12).cuboid(2, 8, 0.5, 14, 10, 4)
            .cuboid(12, 8, 4, 15.5, 10, 12).cuboid(2, 8, 12, 14, 10, 15.5)
            .cuboid(14, 8, 2, 15.5, 10, 4).cuboid(14, 8, 12, 15.5, 10, 14)
            .cuboid(0.5, 8, 12, 2, 10, 14).cuboid(0.5, 8, 2, 2, 10, 4)
            .cuboid(0.25, 8, 14, 2, 9, 15.75).cuboid(14, 8, 14, 15.75, 9, 15.75)
            .cuboid(14, 8, 0.25, 15.75, 9, 2).cuboid(0.25, 8, 0.25, 2, 9, 2)
            .cuboid(5, 8, 5, 11, 10, 11).build();

    public AwakeningAltarBlock(Identifier id) {
        super(id, SoundType.STONE, 10.0F, 12.0F, true);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AwakeningAltarTileEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ALTAR_SHAPE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(ModTooltips.ACTIVATE_WITH_REDSTONE.toComponent());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var tile = level.getBlockEntity(pos);

        if (tile instanceof AwakeningAltarTileEntity altar) {
            var inventory = altar.getInventory();
            var input = inventory.getResource(0);
            var output = inventory.getResource(1);

            if (!output.isEmpty()) {
                var amount = inventory.getAmountAsInt(1);
                var item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), output.toStack(amount));

                item.setNoPickUpDelay();
                level.addFreshEntity(item);
                inventory.set(1, ItemResource.EMPTY, 0);
            } else {
                var held = player.getItemInHand(hand);
                if (input.isEmpty() && !held.isEmpty()) {
                    inventory.set(0, ItemResource.of(held), 1);
                    player.setItemInHand(hand, held.copyWithCount(held.count() - 1));
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (!input.isEmpty()) {
                    if (held.getItem() instanceof WandItem)
                        return InteractionResult.PASS;

                    var amount = inventory.getAmountAsInt(0);
                    var item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), input.toStack(amount));

                    item.setNoPickUpDelay();
                    level.addFreshEntity(item);
                    inventory.set(0, ItemResource.EMPTY, 0);
                }
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

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModTileEntities.AWAKENING_ALTAR, AwakeningAltarTileEntity::tick);
    }
}
