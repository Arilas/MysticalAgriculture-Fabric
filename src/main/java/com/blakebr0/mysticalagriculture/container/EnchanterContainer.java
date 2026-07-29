package com.blakebr0.mysticalagriculture.container;

import com.blakebr0.cucumber.container.BaseContainerMenu;
import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.RecipeInventory;
import com.blakebr0.mysticalagriculture.container.slot.EnchanterOutputSlot;
import com.blakebr0.mysticalagriculture.container.slot.EnchanterSlot;
import com.blakebr0.mysticalagriculture.init.ModMenuTypes;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.tileentity.EnchanterTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnchanterContainer extends BaseContainerMenu {
    private final Level level;
    private final CItemStacksHandler inventory;
    private final Container result;

    public EnchanterContainer(int id, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(id, playerInventory, EnchanterTileEntity.createInventoryHandler(), buffer.readBlockPos());
    }

    public EnchanterContainer(int id, Inventory playerInventory, CItemStacksHandler inventory, BlockPos pos) {
        super(ModMenuTypes.ENCHANTER, id, pos);
        this.level = playerInventory.player.level();
        this.inventory = inventory;
        this.result = new ResultContainer();

        this.addSlot(new EnchanterSlot(this, inventory, 0, 17, 41));
        this.addSlot(new EnchanterSlot(this, inventory, 1, 39, 41));
        this.addSlot(new EnchanterSlot(this, inventory, 2, 79, 41));

        this.addSlot(new EnchanterOutputSlot(this, inventory, this.result, 3, 139, 41));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 95 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 153));
        }

        this.slotsChanged(new RecipeInventory(inventory));
    }

    @Override
    public void slotsChanged(Container matrix) {
        if (this.level instanceof ServerLevel serverLevel) {
            var inventory = this.inventory.toShapelessCraftingInput(0, 3);
            var recipe = serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.ENCHANTER, inventory, this.level);

            if (recipe.isPresent()) {
                this.result.setItem(0, recipe.get().value().assemble(inventory));
            } else {
                this.result.setItem(0, ItemStack.EMPTY);
            }
        }

        super.slotsChanged(matrix);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 3) {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= 4 && index < 40) {
                if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
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
}
