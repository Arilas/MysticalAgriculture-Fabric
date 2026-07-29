package com.blakebr0.mysticalagriculture.tileentity;

import com.blakebr0.cucumber.energy.CEnergyStorage;
import com.blakebr0.cucumber.helper.ItemResourceHelper;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.CachedRecipe;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import com.blakebr0.cucumber.inventory.SidedInventoryWrapper;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.cucumber.util.ContainerDataBuilder;
import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.api.machine.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeTier;
import com.blakebr0.mysticalagriculture.block.ReprocessorBlock;
import com.blakebr0.mysticalagriculture.container.ReprocessorContainer;
import com.blakebr0.mysticalagriculture.handler.MachineItemStorage;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ReprocessorTileEntity extends BaseInventoryTileEntity implements ExtendedMenuProvider<BlockPos>, IUpgradeableMachine {
    private static final int INPUT_SLOT = 0;
    private static final int FUEL_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    public static final int FUEL_TICK_MULTIPLIER = 20;
    public static final int OPERATION_TIME = 200;
    public static final int FUEL_USAGE = 20;
    public static final int FUEL_CAPACITY = 80000;

    private final CItemStacksHandler inventory;
    private final MachineUpgradeItemStackHandler upgradeInventory;
    private final CEnergyStorage energy;
    private final SidedInventoryWrapper[] sidedInventoryWrappers;
    private final CachedRecipe<CraftingInput, IReprocessorRecipe> recipe;

    private final ContainerData dataAccess;

    private MachineUpgradeTier tier;
    private int progress;
    private int fuelLeft;
    private int fuelItemValue;
    private boolean isRunning;

    public ReprocessorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.REPROCESSOR, pos, state);
        this.inventory = createInventoryHandler((_, _) -> this.setChanged(), this::getLevel);
        this.upgradeInventory = new MachineUpgradeItemStackHandler((_, _) -> this.setChanged());
        this.energy = new CEnergyStorage(FUEL_CAPACITY, _ -> this.setChangedFast());
        this.sidedInventoryWrappers = SidedInventoryWrapper.create(this.inventory, List.of(Direction.UP, Direction.DOWN, Direction.NORTH), this::canInsertStackSided, null);
        this.recipe = new CachedRecipe<>(ModRecipeTypes.REPROCESSOR);

        this.dataAccess = ContainerDataBuilder.builder()
                .sync(this.energy::getAmountAsInt, this.energy::set)
                .sync(this.energy::getCapacityAsInt, this.energy::setMaxCapacity)
                .sync(() -> this.progress, value -> this.progress = value)
                .sync(this::getOperationTime)
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
        this.fuelItemValue = input.getIntOr("fuel_item_value", 0);
        this.energy.deserialize(input.childOrEmpty("energy"));
        this.upgradeInventory.deserialize(input.childOrEmpty("upgrade_inventory"));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("progress", this.progress);
        output.putInt("fuel_left", this.fuelLeft);
        output.putInt("fuel_item_value", this.fuelItemValue);
        this.energy.serialize(output.child("energy"));
        this.upgradeInventory.serialize(output.child("upgrade_inventory"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mysticalagriculture.reprocessor");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos().immutable();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ReprocessorContainer(id, playerInventory, this.inventory, this.upgradeInventory, this.dataAccess, this.getBlockPos());
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

    public @Nullable Storage<ItemVariant> getSidedInventory(@Nullable Direction direction) {
        return MachineItemStorage.sided(this.sidedInventoryWrappers, direction);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReprocessorTileEntity tile) {
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

        if (tier != tile.tier) {
            tile.tier = tier;

            if (tier == null) {
                tile.energy.resetMaxCapacity();
            } else {
                tile.energy.setMaxCapacity(FUEL_CAPACITY * tier.getFuelCapacityMultiplier());
            }

            tile.setChangedFast();
        }

        var wasRunning = tile.isRunning;

        if (tile.energy.getAmountAsInt() >= tile.getFuelUsage()) {
            tile.isRunning = false;

            var input = tile.inventory.getResource(INPUT_SLOT);
            if (!input.isBlank()) {
                var recipe = tile.getActiveRecipe();

                if (recipe != null) {
                    var result = recipe.assemble(tile.toCraftingInput());

                    if (ItemResourceHelper.canCombine(tile.inventory, OUTPUT_SLOT, result)) {
                        tile.isRunning = true;
                        tile.progress++;

                        try (var tx = Transaction.openOuter()) {
                            tile.energy.extract(tile.getFuelUsage(), tx);

                            if (tile.progress >= tile.getOperationTime()) {
                                tile.inventory.extract(INPUT_SLOT, input, 1, tx, true);
                                tile.inventory.insert(OUTPUT_SLOT, ItemVariant.of(result), result.count(), tx, true);

                                tile.progress = 0;
                            }

                            tx.commit();
                        }

                        tile.setChangedFast();
                    }
                }
            } else {
                if (tile.progress > 0) {
                    tile.progress = 0;

                    tile.setChangedFast();
                }
            }
        } else {
            tile.isRunning = false;
        }

        if (wasRunning != tile.isRunning) {
            level.setBlock(pos, state.setValue(ReprocessorBlock.RUNNING, tile.isRunning), 3);

            tile.setChangedFast();
        }

        tile.dispatchIfChanged();
    }

    public IReprocessorRecipe getActiveRecipe() {
        if (this.level == null)
            return null;

        return this.recipe.checkAndGet(this.toCraftingInput(), (ServerLevel) this.level);
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

    private CraftingInput toCraftingInput() {
        return this.inventory.toShapelessCraftingInput(0, 1);
    }

    private boolean canInsertStackSided(int slot, ItemVariant resource, Direction direction) {
        var stack = resource.toStack();
        if (direction == null)
            return true;
        if (slot == INPUT_SLOT && direction == Direction.UP)
            return RecipeIngredientCache.INSTANCE.isValidInput(stack, ModRecipeTypes.REPROCESSOR);
        if (slot == FUEL_SLOT && direction == Direction.NORTH)
            return this.level != null && this.level.fuelValues().isFuel(stack);

        return false;
    }

    public static CItemStacksHandler createInventoryHandler() {
        return createInventoryHandler(null, () -> null);
    }

    public static CItemStacksHandler createInventoryHandler(@Nullable OnContentsChangedFunction onContentsChanged, Supplier<Level> level) {
        return CItemStacksHandler.create(3, onContentsChanged, builder -> {
            builder.setCanInsert((slot, resource) -> switch (slot) {
                case FUEL_SLOT -> level.get() != null && level.get().fuelValues().isFuel(resource.toStack());
                case OUTPUT_SLOT -> false;
                default -> true;
            });
            builder.setCanExtract(slot -> switch (slot) {
                case FUEL_SLOT -> level.get() == null || !level.get().fuelValues().isFuel(builder.getResource(slot).toStack());
                case OUTPUT_SLOT -> true;
                default -> false;
            });
        });
    }
}
