package com.blakebr0.mysticalagriculture.lib;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> MYSTICAL_ENLIGHTENMENT = ResourceKey.create(Registries.ENCHANTMENT, MysticalAgriculture.resource("mystical_enlightenment"));
    public static final ResourceKey<Enchantment> SOUL_SIPHONER = ResourceKey.create(Registries.ENCHANTMENT, MysticalAgriculture.resource("soul_siphoner"));

    private ModEnchantments() {
    }

    public static int getLevel(ResourceKey<Enchantment> enchantment, ItemStack stack, RegistryAccess access) {
        var holder = access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }
}
