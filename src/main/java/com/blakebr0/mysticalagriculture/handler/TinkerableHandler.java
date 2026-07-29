package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.cucumber.event.ItemBreakEvent;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TinkerableHandler {
    private TinkerableHandler() {
    }

    public static void register() {
        ItemBreakEvent.EVENT.register(TinkerableHandler::onItemBreak);
    }

    private static void onItemBreak(ItemBreakEvent event) {
        var item = event.getItem();
        var entity = event.getEntity();

        if (item instanceof ITinkerable && entity instanceof Player player) {
            var stack = event.getItemStack();
            var augments = AugmentUtils.getAugments(stack);

            for (var augment : augments) {
                player.getInventory().placeItemBackInInventory(new ItemStack(augment.getItem()));
            }
        }
    }

    public static ItemAttributeModifiers appendAttributeModifiers(
            ItemStack stack,
            ItemAttributeModifiers base
    ) {
        if (!(stack.getItem() instanceof ITinkerable)) {
            return base;
        }

        var result = base;
        for (var augment : AugmentUtils.getAugments(stack)) {
            for (var modifier : augment.getAttributeModifiers()) {
                result = result.withModifierAdded(
                        modifier.attribute(),
                        modifier.modifier(),
                        modifier.slot()
                );
            }
        }

        return result;
    }
}
