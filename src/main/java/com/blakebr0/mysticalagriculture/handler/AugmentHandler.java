package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.mysticalagriculture.api.lib.AbilityCache;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerPlayer;

public final class AugmentHandler {
    private static final AbilityCache ABILITY_CACHE = new AbilityCache();

    private AugmentHandler() {
    }

    public static void register() {
        ServerTickEvents.START_LEVEL_TICK.register(level -> {
            for (var player : level.players()) {
                updatePlayer(player);
            }
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player && source.is(DamageTypeTags.IS_FALL)) {
                return !shouldCancelFallDamage(player, (float) player.fallDistance);
            }
            return true;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, _) -> {
            if (entity instanceof ServerPlayer player) {
                ABILITY_CACHE.removeAll(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) ->
                ABILITY_CACHE.removeAll(handler.getPlayer()));
    }

    static void updatePlayer(ServerPlayer player) {
        if (player.isDeadOrDying()) {
            ABILITY_CACHE.removeAll(player);
            return;
        }

        var augments = AugmentUtils.getArmorAugments(player);

        for (var augment : augments) {
            augment.onPlayerTick(player, ABILITY_CACHE);
        }

        for (var augment : ABILITY_CACHE.getCachedAbilities(player)) {
            if (augments.stream().noneMatch(a -> augment.equals(a.getId().toString()))) {
                ABILITY_CACHE.remove(augment, player);
            }
        }
    }

    static boolean shouldCancelFallDamage(ServerPlayer player, float fallDistance) {
        for (var augment : AugmentUtils.getArmorAugments(player)) {
            if (augment.onPlayerFall(player, fallDistance)) {
                return true;
            }
        }
        return false;
    }
}
