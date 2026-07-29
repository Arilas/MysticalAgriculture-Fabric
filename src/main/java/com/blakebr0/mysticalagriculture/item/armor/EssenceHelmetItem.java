package com.blakebr0.mysticalagriculture.item.armor;

import com.blakebr0.cucumber.item.BaseArmorItem;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Consumer;

public class EssenceHelmetItem extends BaseArmorItem implements ITinkerable {
    private static final EnumSet<AugmentType> TYPES = EnumSet.of(AugmentType.ARMOR, AugmentType.HELMET);
    private final int tinkerableTier;
    private final int slots;

    public EssenceHelmetItem(Identifier id, ArmorMaterial material, int tinkerableTier, int slots) {
        super(id, material, ArmorType.HELMET, p -> {
            p.component(ModDataComponentTypes.EQUIPPED_AUGMENTS, new ArrayList<>(slots));

            // Supremium+ armor can be unbreakable if enabled
            if (tinkerableTier >= 5 && ModConfigs.UNBREAKABLE_SUPREMIUM_ARMOR.get()) {
                p.component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            }

            return p;
        });
        this.tinkerableTier = tinkerableTier;
        this.slots = slots;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        var augments = AugmentUtils.getAugments(stack);

        if (slot == EquipmentSlot.HEAD && entity instanceof Player player) {
            for (var augment : augments) {
                augment.onArmorTick(stack, level, player);
            }
        }

        for (var augment : augments) {
            augment.onInventoryTick(stack, level, entity, slot);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(ModTooltips.getTooltipForTier(this.tinkerableTier));

        if (ModConfigs.AWAKENED_SUPREMIUM_SET_BONUS.get() && stack.is(ModItems.AWAKENED_SUPREMIUM_HELMET)) {
            builder.accept(ModTooltips.SET_BONUS.args(ModTooltips.AWAKENED_SUPREMIUM_SET_BONUS.toComponent()).toComponent());
        }

        AugmentUtils.addAugmentListToTooltip(builder, stack, this.slots, null);
    }

    @Override
    public int getAugmentSlots() {
        return this.slots;
    }

    @Override
    public EnumSet<AugmentType> getAugmentTypes() {
        return TYPES;
    }

    @Override
    public int getTinkerableTier() {
        return this.tinkerableTier;
    }
}
