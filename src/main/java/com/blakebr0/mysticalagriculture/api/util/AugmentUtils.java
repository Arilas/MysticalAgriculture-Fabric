package com.blakebr0.mysticalagriculture.api.util;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureDataComponentTypes;
import com.blakebr0.mysticalagriculture.api.components.AOEAugmentOffsetComponent;
import com.blakebr0.mysticalagriculture.api.components.AugmentComponent;
import com.blakebr0.mysticalagriculture.api.tinkering.AOEAugment;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AugmentUtils {
    /**
     * Add an augment to the specified tinkerable in the specified augment slot
     *
     * @param stack   the {@link ITinkerable} item
     * @param augment the augment
     * @param slot    the augment slot
     */
    public static void addAugment(ItemStack stack, Augment augment, int slot) {
        var item = stack.getItem();

        if (item instanceof ITinkerable tinkerable) {
            if (slot < tinkerable.getAugmentSlots() && tinkerable.getTinkerableTier() >= augment.getTier()) {
                var component = stack.get(MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS);
                var augments = new ArrayList<AugmentComponent>();

                if (component != null && !component.isEmpty()) {
                    augments.addAll(component);
                }

                augments.removeIf(a -> a.slot() == slot);
                augments.add(new AugmentComponent(augment.getId(), slot));

                stack.set(MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS, augments);
            }
        }
    }

    /**
     * Add an augment to the specified tinkerable in the specified augment slot
     *
     * @param resource the {@link ITinkerable} item
     * @param augment  the augment
     * @param slot     the augment slot
     * @return the new {@link ItemVariant} with the added augment
     */
    public static ItemVariant addAugment(ItemVariant resource, Augment augment, int slot) {
        var stack = resource.toStack();
        addAugment(stack, augment, slot);
        return ItemVariant.of(stack);
    }

    /**
     * Remove an augment from the specified tinkerable from the specified augment slot
     *
     * @param stack the {@link ITinkerable} item
     * @param slot  the augment slot
     */
    public static void removeAugment(ItemStack stack, int slot) {
        var component = stack.get(MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS);
        if (component == null)
            return;

        var item = stack.getItem();

        if (item instanceof ITinkerable tinkerable) {
            var augment = getAugment(stack, slot);
            if (slot < tinkerable.getAugmentSlots() && augment != null) {
                var augments = new ArrayList<AugmentComponent>();

                if (!component.isEmpty()) {
                    augments.addAll(component);
                }

                augments.removeIf(a -> a.slot() == slot);

                stack.set(MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS, augments);
                stack.remove(MysticalAgricultureDataComponentTypes.AOE_AUGMENT_OFFSET);
            }
        }
    }

    /**
     * Remove an augment from the specified tinkerable from the specified slot
     *
     * @param resource the {@link ITinkerable} item
     * @param slot     the augment slot
     * @return the {@link ItemVariant} without the removed augment
     */
    public static ItemVariant removeAugment(ItemVariant resource, int slot) {
        var stack = resource.toStack();
        removeAugment(stack, slot);
        return ItemVariant.of(stack);
    }

    /**
     * Get the augment in the specified augment slot
     *
     * @param stack the {@link ITinkerable} item
     * @param slot  the augment slot
     * @return the augment
     */
    public static Augment getAugment(ItemStack stack, int slot) {
        var component = stack.get(MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS);
        if (component == null)
            return null;

        var item = stack.getItem();

        if (item instanceof ITinkerable tinkerable) {
            var augment = component.stream()
                    .filter(a -> a.slot() == slot)
                    .findFirst()
                    .orElse(null);

            if (augment == null)
                return null;

            if (slot < tinkerable.getAugmentSlots()) {
                return MysticalAgricultureAPI.getAugmentRegistry().getAugmentById(augment.id());
            }
        }

        return null;
    }

    /**
     * Get the augment in the specified augment slot
     *
     * @param resource the {@link ITinkerable} item
     * @param slot     the augment slot
     * @return the augment
     */
    public static Augment getAugment(ItemVariant resource, int slot) {
        return getAugment(resource.toStack(), slot);
    }

    /**
     * Gets the augments currently installed on this tinkerable
     *
     * @param stack the {@link ITinkerable}
     * @return the installed augments
     */
    public static List<Augment> getAugments(ItemStack stack) {
        var component = stack.get(MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS);
        List<Augment> augments = new ArrayList<>();

        if (component == null)
            return augments;

        var item = stack.getItem();

        if (item instanceof ITinkerable tinkerable) {
            int slots = tinkerable.getAugmentSlots();

            for (int i = 0; i < slots; i++) {
                var augment = getAugment(stack, i);
                if (augment != null)
                    augments.add(augment);
            }
        }

        return augments;
    }

    /**
     * Gets the augments currently installed on this tinkerable
     *
     * @param resource the {@link ITinkerable}
     * @return the installed augments
     */
    public static List<Augment> getAugments(ItemVariant resource) {
        return getAugments(resource.toStack());
    }

    /**
     * Helper method to get the augments from the player's armor
     *
     * @param player the player
     * @return the installed augments
     */
    public static List<Augment> getArmorAugments(Player player) {
        List<Augment> augments = new ArrayList<>();

        for (var slot : Inventory.EQUIPMENT_SLOT_MAPPING.values()) {
            var stack = player.getItemBySlot(slot);

            augments.addAll(getAugments(stack));
        }

        return augments;
    }

    /**
     * Gets that largest AOE augment size
     *
     * @param stack the {@link ITinkerable}
     * @return the range
     */
    public static int getMaxAOEAugmentRange(ItemStack stack) {
        int range = 0;

        var augments = getAugments(stack);
        for (var augment : augments) {
            if (augment instanceof AOEAugment aoeAugment) {
                range = Math.max(range, aoeAugment.getRange());
            }
        }

        return range;
    }

    /**
     * Used to add the augment tooltip lines to an item
     *
     * @param tooltip the tooltip consumer from {@link Item#appendHoverText}
     * @param stack   the {@link ITinkerable} item
     * @param slots   the number of augment slots this {@link ITinkerable} has
     * @param player  the player
     */
    public static void addAugmentListToTooltip(Consumer<Component> tooltip, ItemStack stack, int slots, Player player) {
        tooltip.accept(Component.translatable("tooltip.mysticalagriculture.augments").withStyle(ChatFormatting.GRAY));

        var augments = AugmentUtils.getAugments(stack);

        for (int i = 0; i < slots; i++) {
            var augment = i < augments.size() ? augments.get(i) : null;
            var name = augment != null ? augment.getDisplayName() : Component.translatable("tooltip.mysticalagriculture.empty");

            if (augment != null && augment.hasSetBonus() && TinkerableUtils.hasArmorSetMinimumTier(player, augment.getTier())) {
                name.withStyle(ChatFormatting.GREEN);
            }

            if (augment instanceof AOEAugment) {
                var offset = stack.getOrDefault(MysticalAgricultureDataComponentTypes.AOE_AUGMENT_OFFSET, AOEAugmentOffsetComponent.DEFAULT);
                if (offset.isOffset()) {
                    var horizontalOffset = String.format("%+d", offset.horizontalOffset());
                    var verticalOffset = String.format("%+d", offset.verticalOffset());

                    name.append(" (").append(Component.translatable("tooltip.mysticalagriculture.aoe_offset", horizontalOffset, verticalOffset).append(")"));
                }
            }

            tooltip.accept(Component.literal(" - ").withStyle(ChatFormatting.GRAY).append(name));
        }
    }
}
