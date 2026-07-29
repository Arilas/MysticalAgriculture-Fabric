package com.blakebr0.mysticalagriculture.tileentity;

import com.blakebr0.cucumber.energy.CEnergyStorage;
import com.blakebr0.cucumber.helper.CropHelper;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.cucumber.util.ContainerDataBuilder;
import com.blakebr0.mysticalagriculture.api.machine.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeTier;
import com.blakebr0.mysticalagriculture.block.HarvesterBlock;
import com.blakebr0.mysticalagriculture.container.HarvesterContainer;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class HarvesterTileEntity extends BaseInventoryTileEntity implements ExtendedMenuProvider<BlockPos>, IUpgradeableMachine {
    private static final int FUEL_SLOT = 0;
    private static final int[] OUTPUT_SLOTS = IntStream.rangeClosed(1, 15).toArray();

    public static final int FUEL_TICK_MULTIPLIER = 20;
    public static final int OPERATION_TIME = 100;
    public static final int FUEL_USAGE = 40;
    public static final int SCAN_FUEL_USAGE = 10;
    public static final int FUEL_CAPACITY = 80000;
    public static final int BASE_RANGE = 1;

    private final CItemStacksHandler inventory;
    private final MachineUpgradeItemStackHandler upgradeInventory;
    private final CEnergyStorage energy;

    private final ContainerData dataAccess;

    private MachineUpgradeTier tier;
    private Direction direction;
    private int progress;
    private int fuelLeft;
    private int fuelItemValue;
    private int lastScanIndex = -1;
    private boolean isRunning;

    public HarvesterTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.HARVESTER, pos, state);
        this.inventory = createInventoryHandler((_, _) -> this.setChanged(), this::getLevel);
        this.upgradeInventory = new MachineUpgradeItemStackHandler((_, _) -> this.setChanged());
        this.energy = new CEnergyStorage(FUEL_CAPACITY, _ -> this.setChangedFast());

        this.dataAccess = ContainerDataBuilder.builder()
                .sync(this.energy::getAmountAsInt, this.energy::set)
                .sync(this.energy::getCapacityAsInt, this.energy::setMaxCapacity)
                .sync(() -> this.fuelLeft, value -> this.fuelLeft = value)
                .sync(() -> this.fuelItemValue, value -> this.fuelItemValue = value)
                .build();
    }

    @Override
    public CItemStacksHandler getInventory() {
        return this.inventory;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.progress = input.getIntOr("progress", 0);
        this.fuelLeft = input.getIntOr("fuel_left", 0);
        this.fuelItemValue = input.getIntOr("fuel_left_value", 0);
        this.lastScanIndex = input.getIntOr("last_scan_index", -1);
        this.energy.deserialize(input.childOrEmpty("energy"));
        this.upgradeInventory.deserialize(input.childOrEmpty("upgrade_inventory"));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("progress", this.progress);
        output.putInt("fuel_left", this.fuelLeft);
        output.putInt("fuel_item_value", this.fuelItemValue);
        output.putInt("last_scan_index", this.lastScanIndex);
        this.energy.serialize(output.child("energy"));
        this.upgradeInventory.serialize(output.child("upgrade_inventory"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mysticalagriculture.harvester");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos().immutable();
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new HarvesterContainer(i, inventory, this.inventory, this.upgradeInventory, this.dataAccess, this.getBlockPos());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (this.level != null) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.upgradeInventory.getStackCopy());
        }
    }

    @Override
    public MachineUpgradeItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    protected void clearAdditional() {
        this.upgradeInventory.clear();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HarvesterTileEntity tile) {
        if (tile.energy.getAmountAsInt() < tile.energy.getCapacityAsInt()) {
            var fuel = tile.inventory.getResource(FUEL_SLOT);

            try (var tx = Transaction.openOuter()) {
                if (tile.fuelLeft <= 0 && !fuel.isBlank()) {
                    tile.fuelItemValue = level.fuelValues().burnDuration(fuel.toStack());

                    if (tile.fuelItemValue > 0) {
                        tile.fuelLeft = tile.fuelItemValue *= FUEL_TICK_MULTIPLIER;
                        tile.inventory.extract(FUEL_SLOT, fuel, 1, tx, true);

                        tile.setChangedFast();
                    }
                }

                if (tile.fuelLeft > 0) {
                    var fuelPerTick = Math.min(Math.min(tile.fuelLeft, tile.getFuelUsage() * 2), tile.energy.getCapacityAsInt() - tile.energy.getAmountAsInt());

                    tile.fuelLeft -= tile.energy.insert(fuelPerTick, tx);

                    if (tile.fuelLeft <= 0)
                        tile.fuelItemValue = 0;

                    tile.setChangedFast();
                }

                tx.commit();
            }
        }

        var tier = tile.getMachineTier();
        var direction = state.getValue(HarvesterBlock.FACING);

        if (tier != tile.tier || direction != tile.direction) {
            tile.tier = tier;
            tile.direction = direction;

            if (tier == null) {
                tile.energy.resetMaxCapacity();
            } else {
                tile.energy.setMaxCapacity(FUEL_CAPACITY * tier.getFuelCapacityMultiplier());
            }

            tile.setChangedFast();
        }

        var isDisabled = level.hasNeighborSignal(tile.getBlockPos());
        var operationTime = tile.getOperationTime();

        if (tile.progress > operationTime && !isDisabled) {
            var nextPos = tile.findNextPosition();
            var cropState = level.getBlockState(nextPos);
            var block = cropState.getBlock();

            if (block instanceof CropBlock crop) {
                var seed = CropHelper.getSeedsItem(block);

                try (var tx = Transaction.openOuter()) {
                    if (seed != null && crop.isMaxAge(cropState)) {
                        var drops = Block.getDrops(cropState, (ServerLevel) level, nextPos, tile);

                        for (var drop : drops) {
                            var item = drop.getItem();

                            if (!drop.isEmpty() && item == seed) {
                                drop.shrink(1);
                                break;
                            }
                        }

                        for (var drop : drops) {
                            if (!drop.isEmpty()) {
                                tile.addItemToInventory(drop, level, nextPos, tx);
                            }
                        }

                        level.setBlockAndUpdate(nextPos, crop.getStateForAge(0));

                        tile.energy.extract(tile.getFuelUsage(), tx);
                    } else {
                        tile.energy.extract(SCAN_FUEL_USAGE, tx);
                    }

                    tx.commit();
                }
            }

            tile.progress = 0;

            tile.setChangedFast();
        }

        var wasRunning = tile.isRunning;

        if (!isDisabled && tile.energy.getAmountAsInt() >= tile.getFuelUsage()) {
            tile.progress++;
            tile.isRunning = true;
            tile.setChangedFast();
        } else {
            tile.isRunning = false;
        }

        if (wasRunning != tile.isRunning) {
            level.setBlock(pos, state.setValue(HarvesterBlock.RUNNING, tile.isRunning), 3);

            tile.setChangedFast();
        }

        tile.dispatchIfChanged();
    }

    public CEnergyStorage getEnergy() {
        return this.energy;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getOperationTime() {
        if (this.tier == null)
            return OPERATION_TIME;

        return (int) (OPERATION_TIME * this.tier.getOperationTimeMultiplier());
    }

    public int getFuelLeft() {
        return this.fuelLeft;
    }

    public int getFuelItemValue() {
        return this.fuelItemValue;
    }

    public int getFuelUsage() {
        if (this.tier == null)
            return FUEL_USAGE;

        return (int) (FUEL_USAGE * this.tier.getFuelUsageMultiplier());
    }

    private BlockPos findNextPosition() {
        var range = tier != null ? BASE_RANGE + tier.getAddedRange() : BASE_RANGE;
        var size = range * 2 + 1;

        var index = this.lastScanIndex + 1;
        if (index >= (int) Math.pow(size, 2)) {
            index = 0;
        }

        this.lastScanIndex = index;

        var xOffset = (index % size) - range;
        var zOffset = (index / size) - range;

        var center = this.getBlockPos().relative(direction, range + 1);

        return switch (direction) {
            case Direction.NORTH -> new BlockPos(center.getX() + xOffset, center.getY(), center.getZ() - zOffset);
            case Direction.SOUTH -> new BlockPos(center.getX() - xOffset, center.getY(), center.getZ() + zOffset);
            case Direction.EAST -> new BlockPos(center.getX() + zOffset, center.getY(), center.getZ() + xOffset);
            case Direction.WEST -> new BlockPos(center.getX() - zOffset, center.getY(), center.getZ() - xOffset);
            default -> center;
        };
    }

    private void addItemToInventory(ItemStack stack, Level level, BlockPos pos, TransactionContext transaction) {
        long remaining = stack.getCount();
        var resource = ItemVariant.of(stack);

        for (int i = 1; i < this.inventory.getContainerSize(); i++) {
            var stackInSlot = this.inventory.getResource(i);

            if (stackInSlot.isBlank() || stackInSlot.equals(resource)) {
                remaining -= this.inventory.insert(i, resource, remaining, transaction, true);
            }

            if (remaining == 0)
                return;
        }

        Block.popResource(level, pos, stack.copyWithCount(Math.toIntExact(remaining)));
    }

    public static CItemStacksHandler createInventoryHandler() {
        return createInventoryHandler(null, () -> null);
    }

    public static CItemStacksHandler createInventoryHandler(@Nullable OnContentsChangedFunction onContentsChanged, Supplier<Level> level) {
        return CItemStacksHandler.create(16, onContentsChanged, builder -> {
            builder.setCanInsert((slot, resource) -> switch (slot) {
                case FUEL_SLOT -> level.get() != null && level.get().fuelValues().isFuel(resource.toStack());
                default -> false;
            });
            builder.setCanExtract(slot -> switch (slot) {
                case FUEL_SLOT -> level.get() == null || !level.get().fuelValues().isFuel(builder.getResource(slot).toStack());
                default -> true;
            });
        });
    }
}
