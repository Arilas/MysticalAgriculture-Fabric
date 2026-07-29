package com.blakebr0.mysticalagriculture.container.slot;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnchanterOutputSlot extends Slot {
    private final AbstractContainerMenu container;
    private final CItemStacksHandler matrix;

    public EnchanterOutputSlot(AbstractContainerMenu container, CItemStacksHandler matrix, Container inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.matrix = matrix;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (player.level().isClientSide())
            return;

        var input = this.matrix.toCraftingInput(2, 1);
        var recipe = ((ServerLevel) player.level()).recipeAccess().getRecipeFor(ModRecipeTypes.ENCHANTER, input, player.level());
        var remaining = recipe.map(r -> r.value().getRemainingItems(input)).orElseGet(NonNullList::create);

        for (int i = 0; i < remaining.size(); i++) {
            var remainder = remaining.get(i);
            this.matrix.set(i, ItemVariant.of(remainder), remainder.count());
        }

        this.container.slotsChanged(this.matrix.toRecipeInventory());
    }
}
