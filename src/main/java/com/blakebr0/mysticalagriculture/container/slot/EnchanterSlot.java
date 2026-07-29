package com.blakebr0.mysticalagriculture.container.slot;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.RecipeInventory;
import com.blakebr0.cucumber.inventory.slot.CSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class EnchanterSlot extends CSlot {
    private final AbstractContainerMenu container;
    private final Container inventory;

    public EnchanterSlot(AbstractContainerMenu container, CItemStacksHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.inventory = new RecipeInventory(inventory);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        this.container.slotsChanged(this.inventory);
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack oldStack) {
        super.setByPlayer(stack, oldStack);
        this.container.slotsChanged(this.inventory);
    }
}
