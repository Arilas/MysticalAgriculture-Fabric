package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;

public class NoFallDamageAugment extends Augment {
    public NoFallDamageAugment(Identifier id, int tier) {
        super(id, tier, EnumSet.of(AugmentType.BOOTS), 0x0092F4, 0x004272);
    }

    @Override
    public boolean onPlayerFall(ServerPlayer player, float fallDistance) {
        return true;
    }
}
