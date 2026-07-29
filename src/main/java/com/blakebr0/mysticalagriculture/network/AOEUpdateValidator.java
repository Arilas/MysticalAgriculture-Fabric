package com.blakebr0.mysticalagriculture.network;

import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.network.payloads.UpdateAOEAugmentOffsetPayload;
import net.minecraft.world.item.ItemStack;

import java.util.function.ToIntFunction;

final class AOEUpdateValidator {
    private AOEUpdateValidator() {
    }

    static boolean isValid(
            ItemStack stack,
            UpdateAOEAugmentOffsetPayload payload,
            ToIntFunction<ItemStack> rangeResolver
    ) {
        return payload.hasOnlySingleStepChanges()
                && !stack.isEmpty()
                && stack.getItem() instanceof ITinkerable
                && rangeResolver.applyAsInt(stack) > 0;
    }
}
