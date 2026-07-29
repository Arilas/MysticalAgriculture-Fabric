package com.blakebr0.mysticalagriculture.container;

import com.blakebr0.cucumber.container.BaseContainerMenu;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.slot.CSlot;
import com.blakebr0.cucumber.util.QuickMover;
import com.blakebr0.mysticalagriculture.api.machine.IMachineUpgrade;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.init.ModMenuTypes;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OreInfuserContainer extends BaseContainerMenu {
    private final ContainerData data;
    private final QuickMover mover;

    public OreInfuserContainer(int id, Inventory playerInventory, CItemStacksHandler inventory, MachineUpgradeItemStackHandler upgradeInventory, ContainerData data, BlockPos pos) {
        super(ModMenuTypes.ORE_INFUSER, id, pos);
        this.data = data;
        this.mover = new QuickMover(this::moveItemStackTo);

        this.addSlot(new CSlot(upgradeInventory, 0, 152, 9));

        this.addSlot(new CSlot(inventory, 0, 60, 52));
        this.addSlot(new CSlot(inventory, 1, 80, 52));
        this.addSlot(new CSlot(inventory, 2, 30, 56));
        this.addSlot(new CSlot(inventory, 3, 140, 52));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 112 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 170));
        }

        this.mover.after(5)
                .add((slot, stack, player) -> stack.getItem() instanceof IMachineUpgrade, 0, 1) // machine upgrade
                .add((slot, stack, player) -> isRecipeInput(stack), 1, 2) // inputs
                .add((slot, stack, player) -> player.level().fuelValues().isFuel(stack), 4, 1) // fuel
                .add((slot, stack, player) -> slot < this.slots.size() - 4, this.slots.size() - 4, 9) // hotbar
                .add((slot, stack, player) -> slot >= this.slots.size() - 4, this.slots.size() - 31, 27); // inventory
        this.mover.fallback(5, 36);

        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (!this.mover.run(index, itemstack1, player)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public int getEnergyStored() {
        return this.data.get(0);
    }

    public int getEnergyCapacity() {
        return this.data.get(1);
    }

    public int getProgress() {
        return this.data.get(2);
    }

    public int getOperationTime() {
        return this.data.get(3);
    }

    public int getFuelLeft() {
        return this.data.get(4);
    }

    public int getFuelItemValue() {
        return this.data.get(5);
    }

    private static boolean isRecipeInput(ItemStack stack) {
        return RecipeIngredientCache.INSTANCE.isValidInput(stack, ModRecipeTypes.ORE_INFUSION);
    }
}
