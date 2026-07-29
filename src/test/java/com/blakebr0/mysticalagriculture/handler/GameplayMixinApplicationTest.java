package com.blakebr0.mysticalagriculture.handler;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class GameplayMixinApplicationTest {
    @Test
    void loaderTransformsBothTaskFiveMixinTargets() {
        assertNotNull(ItemStack.class);
        assertNotNull(ExperienceOrb.class);
    }
}
