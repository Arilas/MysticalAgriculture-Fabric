package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.lib.AbilityCache;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.EnumSet;

public class StepAssistAugment extends Augment {
    private static final Identifier ATTRIBUTE_ID = MysticalAgriculture.resource("step_assist_augment");

    public StepAssistAugment(Identifier id, int tier) {
        super(id, tier, EnumSet.of(AugmentType.LEGGINGS, AugmentType.BOOTS), 0xFC4F00, 0x602600);
    }

    @Override
    public void onPlayerTick(ServerPlayer player, AbilityCache cache) {
        if (!player.isShiftKeyDown() && !cache.isCached(this, player)) {
            var height = player.getAttribute(Attributes.STEP_HEIGHT);
            if (height == null)
                return;

            var hasStepAssist = height.getModifier(ATTRIBUTE_ID) != null;
            if (!hasStepAssist) {
                height.addPermanentModifier(new AttributeModifier(ATTRIBUTE_ID, 1, AttributeModifier.Operation.ADD_VALUE));
            }

            cache.add(this, player, () -> {
                height.removeModifier(ATTRIBUTE_ID);
            });
        }

        if (player.isShiftKeyDown() && cache.isCached(this, player)) {
            cache.remove(this, player);
        }
    }
}
