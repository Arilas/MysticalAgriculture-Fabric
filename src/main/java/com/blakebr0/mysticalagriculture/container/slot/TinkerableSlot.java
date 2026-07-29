package com.blakebr0.mysticalagriculture.container.slot;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.slot.CSlot;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class TinkerableSlot extends CSlot {
    private final AbstractContainerMenu container;
    private final CItemStacksHandler inventory;

    public TinkerableSlot(AbstractContainerMenu container, CItemStacksHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.inventory = inventory;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        var inventory = this.inventory;
        try (var tx = Transaction.openOuter()) {
            for (int i = 0; i < 2; i++) {
                var resource = inventory.getResource(i + 1);
                if (resource.isBlank())
                    continue;

                inventory.extract(i + 1, resource, 1, tx);
            }

            tx.commit();
        }
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack oldStack) {
        var inventory = this.inventory;
        try (var tx = Transaction.openOuter()) {
            for (int i = 0; i < 2; i++) {
                var augmentStack = inventory.getResource(i + 1);
                if (!augmentStack.isBlank()) {
                    inventory.extract(i + 1, augmentStack, inventory.getAmountAsLong(i + 1), tx);
                }

                var augment = AugmentUtils.getAugment(stack, i);
                if (augment != null) {
                    inventory.insert(i + 1, ItemVariant.of(augment.getItem()), stack.count(), tx);
                }
            }

            tx.commit();
        }

        super.setByPlayer(stack, oldStack);
        this.container.slotsChanged(null);
    }
}
