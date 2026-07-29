package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.mysticalagriculture.api.soul.ISoulSiphoningItem;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.item.SoulJarItem;
import com.blakebr0.mysticalagriculture.lib.ModEnchantments;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import java.util.List;
import java.util.stream.Collectors;

public final class MobSoulHandler {
    private MobSoulHandler() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(MobSoulHandler::onLivingDeath);
    }

    private static void onLivingDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        var source = damageSource.getEntity();

        if (source instanceof Player player) {
            var held = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (held.getItem() instanceof ISoulSiphoningItem siphoner) {
                var type = MobSoulTypeRegistry.getInstance().getMobSoulTypeByEntity(entity);

                if (type == null || !type.isEnabled())
                    return;

                var jars = getValidSoulJars(player, type);

                if (!jars.isEmpty()) {
                    double remaining = getSoulSiphonerTotal(siphoner, held, entity);

                    for (var jar : jars) {
                        remaining = MobSoulUtils.addSoulsToJar(jar, type, remaining);
                        if (remaining <= 0)
                            break;
                    }
                }
            }
        }
    }

    private static List<ItemStack> getValidSoulJars(Player player, MobSoulType type) {
        return player.getInventory().getNonEquipmentItems()
                .stream()
                .filter(s -> s.getItem() instanceof SoulJarItem)
                .filter(s -> MobSoulUtils.canAddTypeToJar(s, type))
                .sorted((a, b) -> MobSoulUtils.getType(a) != null ? -1 : MobSoulUtils.getType(b) != null ? 0 : 1)
                .collect(Collectors.toList());
    }

    private static double getSoulSiphonerTotal(ISoulSiphoningItem siphoner, ItemStack stack, LivingEntity entity) {
        double amount = siphoner.getSiphonAmount(stack, entity);

        var enchantmentLevel = ModEnchantments.getLevel(
                ModEnchantments.SOUL_SIPHONER,
                stack,
                entity.level().registryAccess());
        if (enchantmentLevel > 0) {
            amount *= (1.0D + (0.1D * enchantmentLevel));
        }

        return amount;
    }
}
