package com.blakebr0.mysticalagriculture.item;

import com.blakebr0.cucumber.item.tool.BaseSwordItem;
import com.blakebr0.mysticalagriculture.api.soul.ISoulSiphoningItem;
import com.blakebr0.mysticalagriculture.lib.ModToolMaterials;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SouliumDaggerItem extends BaseSwordItem implements ISoulSiphoningItem {
    private final DaggerType type;

    public SouliumDaggerItem(Identifier id, ToolMaterial material, DaggerType type) {
        super(id, material, p -> p.overrideDescription("item.mysticalagriculture.soulium_dagger"));
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        switch (this.type) {
            case PASSIVE -> {
                builder.accept(ModTooltips.PASSIVE_ATTUNED.color(ChatFormatting.GREEN).toComponent());
                builder.accept(ModTooltips.PASSIVE_SOULIUM_DAGGER.toComponent());
            }
            case HOSTILE -> {
                builder.accept(ModTooltips.HOSTILE_ATTUNED.color(ChatFormatting.RED).toComponent());
                builder.accept(ModTooltips.HOSTILE_SOULIUM_DAGGER.toComponent());
            }
            case CREATIVE -> {
                builder.accept(ModTooltips.CREATIVE_ATTUNED.color(ChatFormatting.LIGHT_PURPLE).toComponent());
                builder.accept(ModTooltips.CREATIVE_SOULIUM_DAGGER.toComponent());
            }
        }
    }

    @Override
    public double getSiphonAmount(ItemStack stack, LivingEntity entity) {
        return this.type.getSiphonAmount(stack, entity);
    }

    public enum DaggerType {
        BASIC(3, ModToolMaterials.SOULIUM.durability(), (_, _) -> 1.0D),
        PASSIVE(6, ModToolMaterials.SOULIUM.durability() * 2, (_, entity) -> isPassive(entity) ? 1.5D : 1.0D),
        HOSTILE(6, ModToolMaterials.SOULIUM.durability() * 2, (_, entity) -> !isPassive(entity) ? 1.5D : 1.0D),
        CREATIVE(65, -1, (_, _) -> Double.MAX_VALUE);

        private final int damage;
        private final int durability;
        private final BiFunction<ItemStack, LivingEntity, Double> siphonAmountFunc;

        DaggerType(int damage, int durability, BiFunction<ItemStack, LivingEntity, Double> siphonAmountFunc) {
            this.damage = damage;
            this.durability = durability;
            this.siphonAmountFunc = siphonAmountFunc;
        }

        public double getSiphonAmount(ItemStack stack, LivingEntity entity) {
            return this.siphonAmountFunc.apply(stack, entity);
        }

        public int getDamage() {
            return this.damage;
        }

        public int getDurability() {
            return this.durability;
        }

        private static boolean isPassive(LivingEntity entity) {
            return entity.getType().getCategory().isFriendly();
        }
    }
}
