package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.cucumber.lib.Tooltips;
import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;

public final class MachineUpgradeTooltipHandler {
    private MachineUpgradeTooltipHandler() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, _, _, lines) -> {
            if (stack.getItem() instanceof MachineUpgradeItem upgrade
                    && Minecraft.getInstance().hasShiftDown()) {
                lines.remove(Tooltips.HOLD_SHIFT_FOR_INFO.toComponent());
                upgrade.appendUpgradeDetails(lines::add);
            }
        });
    }
}
