package com.blakebr0.mysticalagriculture.mixin;

import com.blakebr0.mysticalagriculture.handler.ExperienceCapsuleHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Shadow
    public abstract int getValue();

    @Shadow
    private void setValue(int value) {
        throw new AssertionError();
    }

    /**
     * Fabric has no callback around the orb/player pickup transaction. Injecting at the
     * method head lets capsules consume the orb before vanilla applies pickup delay,
     * mending, or player experience.
     */
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true, require = 1)
    private void mysticalagriculture$absorbExperience(Player player, CallbackInfo callback) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.takeXpDelay != 0) {
            return;
        }

        var value = this.getValue();
        var remaining = ExperienceCapsuleHandler.absorbExperience(serverPlayer, value);
        if (remaining == value) {
            return;
        }

        this.setValue(remaining);
        if (remaining == 0) {
            ((ExperienceOrb) (Object) this).discard();
            callback.cancel();
        }
    }
}
