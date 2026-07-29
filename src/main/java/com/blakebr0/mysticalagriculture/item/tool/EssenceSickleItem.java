package com.blakebr0.mysticalagriculture.item.tool;

import com.blakebr0.cucumber.item.tool.BaseSickleItem;
import com.blakebr0.mysticalagriculture.api.components.AOEAugmentOffsetComponent;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Consumer;

public class EssenceSickleItem extends BaseSickleItem implements ITinkerable {
    private static final EnumSet<AugmentType> TYPES = EnumSet.of(AugmentType.TOOL, AugmentType.WEAPON, AugmentType.SICKLE);
    private final int range;
    private final ChatFormatting textColor;
    private final int tinkerableTier;
    private final int slots;

    public EssenceSickleItem(Identifier id, ToolMaterial material, int range, ChatFormatting textColor, int tinkerableTier, int slots) {
        super(id, material, range, p -> {
            p.component(ModDataComponentTypes.EQUIPPED_AUGMENTS, new ArrayList<>(slots));
            p.component(ModDataComponentTypes.AOE_AUGMENT_OFFSET, new AOEAugmentOffsetComponent(0, 0));

            var uses = material.durability();
            if (uses == 0) {
                p.component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            }

            return p;
        });
        this.range = range;
        this.textColor = textColor;
        this.tinkerableTier = tinkerableTier;
        this.slots = slots;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var augments = AugmentUtils.getAugments(context.getItemInHand());
        var success = false;

        for (var augment : augments) {
            if (augment.onItemUse(context))
                success = true;
        }

        if (success)
            return InteractionResult.SUCCESS;

        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var augments = AugmentUtils.getAugments(stack);
        var success = false;

        for (var augment : augments) {
            if (augment.onRightClick(stack, level, player, hand))
                success = true;
        }

        if (success)
            return InteractionResult.SUCCESS;

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        var augments = AugmentUtils.getAugments(stack);
        var success = false;

        for (var augment : augments) {
            if (augment.onRightClickEntity(stack, player, target, hand))
                success = true;
        }

        return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var augments = AugmentUtils.getAugments(stack);

        for (var augment : augments) {
            augment.onHitEntity(stack, target, attacker);
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        var augments = AugmentUtils.getAugments(stack);
        var success = super.mineBlock(stack, level, state, pos, entity);

        for (var augment : augments) {
            if (augment.onBlockDestroyed(stack, level, state, pos, entity))
                success = true;
        }

        return success;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        var augments = AugmentUtils.getAugments(stack);

        for (var augment : augments) {
            augment.onInventoryTick(stack, level, entity, slot);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(ModTooltips.getTooltipForTier(this.tinkerableTier));

        var rangeString = String.valueOf(this.range * 2 + 1);
        var rangeNumber = Component.literal(rangeString + "x" + rangeString).withStyle(this.textColor);

        builder.accept(ModTooltips.TOOL_AREA.args(rangeNumber).toComponent());

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
