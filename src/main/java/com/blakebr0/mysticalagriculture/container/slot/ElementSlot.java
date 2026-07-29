package com.blakebr0.mysticalagriculture.container.slot;

import com.blakebr0.cucumber.iface.IToggleableSlot;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.slot.CSlot;
import com.blakebr0.mysticalagriculture.api.tinkering.IElementalItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ElementSlot extends CSlot implements IToggleableSlot {
    private final AbstractContainerMenu container;
    private final CItemStacksHandler inventory;

    public ElementSlot(AbstractContainerMenu container, CItemStacksHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.inventory = inventory;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        this.container.slotsChanged(null);
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack oldStack) {
        super.setByPlayer(stack, oldStack);
        this.container.slotsChanged(null);
    }

    @Override
    public boolean isActive() {
        var stack = this.inventory.getResource(0);
        var item = stack.getItem();

        return item instanceof IElementalItem;
    }
}
