package com.blakebr0.mysticalagriculture.item;

import com.blakebr0.cucumber.item.BaseWateringCanItem;
import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmlandBlock;

import java.util.function.Function;

public class WateringCanItem extends BaseWateringCanItem {
    public WateringCanItem(Identifier id, int range, double chance) {
        super(id, range, chance);
    }

    public WateringCanItem(Identifier id, int range, double chance, Function<Properties, Properties> properties) {
        super(id, range, chance, properties);
    }

    /**
     * Cucumber's shared implementation still references a removed Fabric API
     * FakePlayer class and only protects the clicked position. Keep the watering
     * transaction here so every mutated position observes server permissions.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null)
            return InteractionResult.FAIL;

        var stack = player.getItemInHand(context.getHand());
        var pos = context.getClickedPos();
        var direction = context.getClickedFace();

        if (isFabricFakePlayer(player)) {
            return this.doWater(stack, context.getLevel(), player, pos, direction);
        }

        if (!context.getLevel().mayInteract(player, pos)
                || !player.mayUseItemAt(pos.relative(direction), direction, stack))
            return InteractionResult.FAIL;

        if (!isFilled(stack))
            return InteractionResult.PASS;

        player.startUsingItem(context.getHand());
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult doWater(
            ItemStack stack,
            Level level,
            Player player,
            BlockPos pos,
            Direction direction
    ) {
        if (!mayWater(level, player, stack, pos, direction))
            return InteractionResult.FAIL;

        if (!isFilled(stack))
            return InteractionResult.FAIL;

        if (!this.allowFakePlayerWatering() && isFabricFakePlayer(player))
            return InteractionResult.FAIL;

        if (!level.isClientSide()) {
            var cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(stack))
                return InteractionResult.FAIL;

            cooldowns.addCooldown(stack, isFabricFakePlayer(player) ? 10 : 5);
        }

        int radius = (this.range - 1) / 2;
        BlockPos.betweenClosedStream(
                pos.offset(-radius, -radius, -radius),
                pos.offset(radius, radius, radius)
        ).forEach(aoePos -> {
            if (!mayWater(level, player, stack, aoePos, direction))
                return;

            var state = level.getBlockState(aoePos);
            if (state.getBlock() instanceof FarmlandBlock
                    && state.getValue(FarmlandBlock.MOISTURE) < 7) {
                level.setBlock(aoePos, state.setValue(FarmlandBlock.MOISTURE, 7), 3);
            }
        });

        var random = Utils.RANDOM;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                var particlePos = pos.offset(x, 0, z);
                if (!mayWater(level, player, stack, particlePos, direction))
                    continue;

                double particleX = particlePos.getX() + random.nextFloat();
                double particleY = particlePos.getY() + 1.0D;
                double particleZ = particlePos.getZ() + random.nextFloat();
                var state = level.getBlockState(particlePos);
                if (state.canOcclude() || state.getBlock() instanceof FarmlandBlock)
                    particleY += 0.3D;

                level.addParticle(
                        ParticleTypes.RAIN,
                        particleX,
                        particleY,
                        particleZ,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }

        startPlayingSound(player);

        if (!level.isClientSide() && Math.random() <= this.chance) {
            BlockPos.betweenClosedStream(
                    pos.offset(-radius, -3, -radius),
                    pos.offset(radius, 3, radius)
            ).forEach(aoePos -> {
                if (!mayWater(level, player, stack, aoePos, direction))
                    return;

                var state = level.getBlockState(aoePos);
                var block = state.getBlock();
                if (block instanceof BonemealableBlock
                        || block == Blocks.MYCELIUM
                        || block == Blocks.CHORUS_FLOWER) {
                    state.randomTick((ServerLevel) level, aoePos.immutable(), random);
                }
            });
        }

        return InteractionResult.FAIL;
    }

    @Override
    protected boolean allowFakePlayerWatering() {
        return ModConfigs.FAKE_PLAYER_WATERING.get();
    }

    private static boolean mayWater(
            Level level,
            Player player,
            ItemStack stack,
            BlockPos pos,
            Direction direction
    ) {
        return level.mayInteract(player, pos)
                && player.mayUseItemAt(pos.relative(direction), direction, stack);
    }

    private static boolean isFabricFakePlayer(Player player) {
        // Fabric has no canonical fake-player class in 26.2. Automation mods use
        // ServerPlayer subclasses; real connected players have the base runtime type.
        return player instanceof ServerPlayer && player.getClass() != ServerPlayer.class;
    }
}
