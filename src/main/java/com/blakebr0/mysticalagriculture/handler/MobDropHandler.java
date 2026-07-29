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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class MobDropHandler {
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();
        var level = (ServerLevel) entity.level();

        if (!level.getGameRules().get(GameRules.MOB_DROPS))
            return;

        var drops = event.getDrops();
        var attacker = event.getSource().getEntity();
        double inferiumDropChance = ModConfigs.INFERIUM_DROP_CHANCE.get();

        if (entity instanceof PathfinderMob && Math.random() < inferiumDropChance) {
            drops.add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.INFERIUM_ESSENCE)));
        }

        if (attacker instanceof Player player) {
            var held = player.getMainHandItem();
            var item = held.getItem();

            if (item instanceof ITinkerable tinkerable) {
                boolean witherDropsEssence = ModConfigs.WITHER_DROPS_ESSENCE.get();

                if (witherDropsEssence && entity instanceof WitherBoss) {
                    var stack = getEssenceForTinkerable(tinkerable, 1, 3);

                    if (!stack.isEmpty()) {
                        drops.add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }

                boolean dragonDropsEssence = ModConfigs.DRAGON_DROPS_ESSENCE.get();

                if (dragonDropsEssence && entity instanceof EnderDragon) {
                    var stack = getEssenceForTinkerable(tinkerable, 2, 4);

                    if (!stack.isEmpty()) {
                        drops.add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }

                var enlightenmentLevel = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.MYSTICAL_ENLIGHTENMENT, held);

                if (enlightenmentLevel > 0) {
                    boolean witherDropsCognizant = ModConfigs.WITHER_DROPS_COGNIZANT.get();

                    if (witherDropsCognizant && entity instanceof WitherBoss) {
                        var stack = new ItemStack(ModItems.COGNIZANT_DUST, 4 + (enlightenmentLevel - 1));

                        drops.add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }

                    boolean dragonDropsCognizant = ModConfigs.DRAGON_DROPS_COGNIZANT.get();

                    if (dragonDropsCognizant && entity instanceof EnderDragon) {
                        var stack = new ItemStack(ModItems.COGNIZANT_DUST, 4 + (enlightenmentLevel * 2));

                        drops.add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }
            }
        }
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
