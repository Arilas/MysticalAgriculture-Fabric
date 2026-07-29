package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.mysticalagriculture.api.lib.AbilityCache;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;

public class FlightAugment extends Augment {
    public FlightAugment(Identifier id, int tier) {
        super(id, tier, EnumSet.of(AugmentType.CHESTPLATE), 0xCBD6D6, 0x556B6B);
    }

    @Override
    public void onPlayerTick(ServerPlayer player, AbilityCache cache) {
        var abilities = player.getAbilities();

        if (cache.isCached(this, player) && !abilities.mayfly) {
            cache.removeQuietly(this, player);
        }

        if (!cache.isCached(this, player)) {
            var couldAlreadyFly = abilities.mayfly;
            abilities.mayfly = true;
            player.onUpdateAbilities();

            cache.add(this, player, () -> {
                if (!couldAlreadyFly && !abilities.instabuild && !player.isSpectator()) {
                    abilities.mayfly = false;
                    abilities.flying = false;
                    player.onUpdateAbilities();
                }
            });
        }
    }
}
