package com.blakebr0.mysticalagriculture.augment;

import com.blakebr0.cucumber.helper.ColorHelper;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class AbsorptionAugment extends Augment {
    private static final AttachmentType<Long> LAST_APPLICATION_TIME = AttachmentRegistry.createPersistent(
            MysticalAgriculture.resource("absorption_augment_last_application"),
            Codec.LONG);
    private final int amplifier;

    public AbsorptionAugment(Identifier id, int tier, int amplifier) {
        super(id, tier, EnumSet.of(AugmentType.ARMOR), getColor(0x7E95A5, tier), getColor(0x3AAAC4, tier));
        this.amplifier = amplifier;
    }

    @Override
    public void onArmorTick(ItemStack stack, ServerLevel level, Player player) {
        if (getLastApplicationTime(player) + 9600L < level.getGameTime()) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 9600, this.amplifier, true, false));
            setLastApplicationTime(player, level.getGameTime());
        }
    }

    private static int getColor(int color, int tier) {
        return ColorHelper.saturate(color, Math.min((float) tier / 5, 1));
    }

    private static long getLastApplicationTime(Player player) {
        return player.getAttachedOrElse(LAST_APPLICATION_TIME, 0L);
    }

    private static void setLastApplicationTime(Player player, long time) {
        player.setAttached(LAST_APPLICATION_TIME, time);
    }
}
