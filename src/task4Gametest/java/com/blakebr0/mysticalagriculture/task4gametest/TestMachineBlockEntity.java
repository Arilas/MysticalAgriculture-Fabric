package com.blakebr0.mysticalagriculture.task4gametest;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.SidedInventoryWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

final class TestMachineBlockEntity extends BlockEntity {
    static final BlockEntityType<TestMachineBlockEntity> TYPE = new BlockEntityType<>(
            TestMachineBlockEntity::new,
            Set.of(Blocks.BARRIER)
    );

    private final AtomicInteger notifications = new AtomicInteger();
    private final CItemStacksHandler inventory = CItemStacksHandler.create(
            3,
            (_, _) -> this.notifications.incrementAndGet(),
            handler -> {
                handler.addSlotLimit(0, 512);
                handler.setOutputSlots(2);
            }
    );
    private final SidedInventoryWrapper[] sidedInventoryWrappers = SidedInventoryWrapper.create(
            this.inventory,
            List.of(Direction.UP, Direction.DOWN, Direction.NORTH),
            this::canInsert,
            this::canExtract
    );

    TestMachineBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    CItemStacksHandler inventory() {
        return this.inventory;
    }

    SidedInventoryWrapper[] sidedInventoryWrappers() {
        return this.sidedInventoryWrappers;
    }

    int notifications() {
        return this.notifications.get();
    }

    private boolean canInsert(int slot, net.fabricmc.fabric.api.transfer.v1.item.ItemVariant stack, Direction side) {
        return switch (side) {
            case UP -> slot == 0;
            case DOWN -> false;
            default -> slot == 1 && stack.isOf(Items.COAL);
        };
    }

    private boolean canExtract(int slot, Direction side) {
        return side == Direction.DOWN && slot == 2;
    }
}
