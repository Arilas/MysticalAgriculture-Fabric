package com.blakebr0.mysticalagriculture.api.machine;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A single-slot transactional inventory for {@link IMachineUpgrade}s.
 */
public class MachineUpgradeItemStackHandler extends CItemStacksHandler {
    public MachineUpgradeItemStackHandler() {
        this(null);
    }

    public MachineUpgradeItemStackHandler(@Nullable OnContentsChangedFunction onContentsChanged) {
        super(1, onContentsChanged);
        this.setDefaultSlotLimit(1);
        this.setCanInsert((_, resource) -> resource.getItem() instanceof IMachineUpgrade);
    }

    /**
     * Gets the {@link MachineUpgradeTier} for the upgrade in this inventory, or null if empty.
     */
    @Nullable
    public MachineUpgradeTier getUpgradeTier() {
        var item = this.getItem(0).getItem();
        return item instanceof IMachineUpgrade upgrade ? upgrade.getTier() : null;
    }

    public ItemStack getStack() {
        return this.getItem(0);
    }

    public ItemStack getStackCopy() {
        return this.getItem(0).copy();
    }

    public void setStack(ItemStack stack) {
        this.setItem(0, stack);
    }

    public void clear() {
        this.clearContent();
    }
}
