package com.blakebr0.mysticalagriculture.item;

import com.blakebr0.cucumber.item.BaseItem;
import com.blakebr0.cucumber.lib.Tooltips;
import com.blakebr0.cucumber.util.Formatting;
import com.blakebr0.mysticalagriculture.api.machine.IMachineUpgrade;
import com.blakebr0.mysticalagriculture.api.machine.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.api.machine.MachineUpgradeTier;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.function.Consumer;

public class MachineUpgradeItem extends BaseItem implements IMachineUpgrade {
    private final MachineUpgradeTier tier;

    public MachineUpgradeItem(Identifier id, MachineUpgradeTier tier) {
        super(id);
        this.tier = tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var tile = level.getBlockEntity(pos);

        if (tile instanceof IUpgradeableMachine machine && machine.canApplyUpgrade(this.tier)) {
            var stack = context.getItemInHand();
            var remaining = machine.applyUpgrade(this);

            stack.shrink(1);

            if (!remaining.isEmpty()) {
                var item = new ItemEntity(level, pos.getX(), pos.getY() + 1, pos.getZ(), remaining.copy());

                level.addFreshEntity(item);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Tooltips.HOLD_SHIFT_FOR_INFO.toComponent());
    }

    public void appendUpgradeDetails(Consumer<Component> builder) {
        var speed = Formatting.number(1 / this.tier.getOperationTimeMultiplier()).withStyle(this.tier.getTextColor());
        var fuelRate = Formatting.number(this.tier.getFuelUsageMultiplier()).withStyle(this.tier.getTextColor());
        var fuelCapacity = Formatting.number(this.tier.getFuelCapacityMultiplier()).withStyle(this.tier.getTextColor());
        var area = Formatting.number(this.tier.getAddedRange()).withStyle(this.tier.getTextColor());

        builder.accept(ModTooltips.UPGRADE_SPEED.args(speed).toComponent());
        builder.accept(ModTooltips.UPGRADE_FUEL_RATE.args(fuelRate).toComponent());
        builder.accept(ModTooltips.UPGRADE_FUEL_CAPACITY.args(fuelCapacity).toComponent());
        builder.accept(ModTooltips.UPGRADE_AREA.args(area).toComponent());
    }

    @Override
    public MachineUpgradeTier getTier() {
        return this.tier;
    }
}
