package com.blakebr0.mysticalagriculture.mixin;

import com.blakebr0.mysticalagriculture.handler.TinkerableHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public abstract class ItemStackAttributeModifiersMixin {
    /**
     * Fabric has no callback for computed stack attribute modifiers. Replace the
     * per-call local value with a new component containing installed augment entries;
     * the shared vanilla component is never mutated.
     */
    @ModifyVariable(
            method = {
                    "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
                    "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
            },
            at = @At("STORE"),
            ordinal = 0,
            require = 1
    )
    private ItemAttributeModifiers mysticalagriculture$appendAugmentModifiers(
            ItemAttributeModifiers base
    ) {
        return TinkerableHandler.appendAttributeModifiers((ItemStack) (Object) this, base);
    }
}
