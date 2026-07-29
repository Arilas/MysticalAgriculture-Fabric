package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.lib.ModEnchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

public final class MobDropHandler {
    private MobDropHandler() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(MobDropHandler::onLivingDeath);
    }

    private static void onLivingDeath(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (!level.getGameRules().get(GameRules.MOB_DROPS))
            return;

        var attacker = source.getEntity();
        double inferiumDropChance = ModConfigs.INFERIUM_DROP_CHANCE.get();

        if (entity instanceof PathfinderMob && Math.random() < inferiumDropChance) {
            spawnDrop(level, entity, new ItemStack(ModItems.INFERIUM_ESSENCE));
        }

        if (attacker instanceof Player player) {
            var held = player.getMainHandItem();
            var item = held.getItem();

            if (item instanceof ITinkerable tinkerable) {
                boolean witherDropsEssence = ModConfigs.WITHER_DROPS_ESSENCE.get();

                if (witherDropsEssence && entity instanceof WitherBoss) {
                    var stack = getEssenceForTinkerable(tinkerable, 1, 3);

                    if (!stack.isEmpty()) {
                        spawnDrop(level, entity, stack);
                    }
                }

                boolean dragonDropsEssence = ModConfigs.DRAGON_DROPS_ESSENCE.get();

                if (dragonDropsEssence && entity instanceof EnderDragon) {
                    var stack = getEssenceForTinkerable(tinkerable, 2, 4);

                    if (!stack.isEmpty()) {
                        spawnDrop(level, entity, stack);
                    }
                }

                var enlightenmentLevel = ModEnchantments.getLevel(
                        ModEnchantments.MYSTICAL_ENLIGHTENMENT,
                        held,
                        level.registryAccess());

                if (enlightenmentLevel > 0) {
                    boolean witherDropsCognizant = ModConfigs.WITHER_DROPS_COGNIZANT.get();

                    if (witherDropsCognizant && entity instanceof WitherBoss) {
                        var stack = new ItemStack(ModItems.COGNIZANT_DUST, 4 + (enlightenmentLevel - 1));

                        spawnDrop(level, entity, stack);
                    }

                    boolean dragonDropsCognizant = ModConfigs.DRAGON_DROPS_COGNIZANT.get();

                    if (dragonDropsCognizant && entity instanceof EnderDragon) {
                        var stack = new ItemStack(ModItems.COGNIZANT_DUST, 4 + (enlightenmentLevel * 2));

                        spawnDrop(level, entity, stack);
                    }
                }
            }
        }
    }

    private static void spawnDrop(ServerLevel level, net.minecraft.world.entity.LivingEntity entity, ItemStack stack) {
        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack));
    }

    private static ItemStack getEssenceForTinkerable(ITinkerable tinkerable, int min, int max) {
        return switch (tinkerable.getTinkerableTier()) {
            case 1 -> new ItemStack(ModItems.INFERIUM_ESSENCE, Utils.randInt(min, max));
            case 2 -> new ItemStack(ModItems.PRUDENTIUM_ESSENCE, Utils.randInt(min, max));
            case 3 -> new ItemStack(ModItems.TERTIUM_ESSENCE, Utils.randInt(min, max));
            case 4 -> new ItemStack(ModItems.IMPERIUM_ESSENCE, Utils.randInt(min, max));
            case 5 -> new ItemStack(ModItems.SUPREMIUM_ESSENCE, Utils.randInt(min, max));
            default -> ItemStack.EMPTY;
        };
    }
}
