package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.mysticalagriculture.api.util.ExperienceCapsuleUtils;
import com.blakebr0.mysticalagriculture.item.ExperienceCapsuleItem;
import com.blakebr0.mysticalagriculture.network.payloads.ExperienceCapsulePickupPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ExperienceCapsuleHandler {
    private ExperienceCapsuleHandler() {
    }

    public static void register() {
        // ExperienceOrbMixin supplies the missing Fabric callback at the pickup transaction.
    }

    public static int absorbExperience(ServerPlayer player, int experience) {
        var remaining = experience;
        var capsules = getExperienceCapsules(player);

        for (var stack : capsules) {
            remaining = ExperienceCapsuleUtils.addExperienceToCapsule(stack, remaining);
            if (remaining == 0) {
                ServerPlayNetworking.send(player, new ExperienceCapsulePickupPayload());
                break;
            }
        }

        return remaining;
    }

    private static List<ItemStack> getExperienceCapsules(Player player) {
        var items = new ArrayList<ItemStack>();

        var stack = player.getOffhandItem();
        if (stack.getItem() instanceof ExperienceCapsuleItem)
            items.add(stack);

        player.getInventory().getNonEquipmentItems()
                .stream()
                .filter(s -> s.getItem() instanceof ExperienceCapsuleItem)
                .forEach(items::add);

        return items;
    }
}
