package com.blakebr0.mysticalagriculture.gametest;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.lib.AbilityCache;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.api.util.ExperienceCapsuleUtils;
import com.blakebr0.mysticalagriculture.augment.FlightAugment;
import com.blakebr0.mysticalagriculture.handler.ExperienceCapsuleHandler;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.item.MysticalFertilizerItem;
import com.blakebr0.mysticalagriculture.item.WateringCanItem;
import com.blakebr0.mysticalagriculture.item.armor.EssenceChestplateItem;
import com.blakebr0.mysticalagriculture.lib.ModAugments;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicBoolean;

public final class GameplayGameTests {
    @GameTest
    public void experienceCapsuleAbsorbsOrbValueBeforeVanillaExperience(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var capsule = new ItemStack(ModItems.EXPERIENCE_CAPSULE);
        player.setItemInHand(InteractionHand.OFF_HAND, capsule);

        var remaining = ExperienceCapsuleHandler.absorbExperience(
                player,
                ExperienceCapsuleUtils.MAX_XP_POINTS + 1
        );

        require(remaining == 1, "the capsule consumed more experience than its capacity");
        require(
                ExperienceCapsuleUtils.getExperience(capsule) == ExperienceCapsuleUtils.MAX_XP_POINTS,
                "the capsule did not retain the absorbed orb experience"
        );
        require(player.totalExperience == 0, "orb experience leaked into the player");
        helper.succeed();
    }

    @GameTest
    public void mysticalFertilizerMaturesCropAndConsumesOneItem(GameTestHelper helper) {
        var position = new BlockPos(1, 1, 1);
        helper.setBlock(position, Blocks.WHEAT.defaultBlockState());
        var stack = new ItemStack(ModItems.MYSTICAL_FERTILIZER, 2);

        require(
                MysticalFertilizerItem.applyFertilizer(
                        stack,
                        helper.getLevel(),
                        helper.absolutePos(position),
                        null
                ),
                "mystical fertilizer rejected a valid crop"
        );

        var state = helper.getLevel().getBlockState(helper.absolutePos(position));
        require(state.getValue(CropBlock.AGE) == CropBlock.MAX_AGE, "fertilizer did not mature the crop");
        require(stack.getCount() == 1, "fertilizer did not consume exactly one item");
        helper.succeed();
    }

    @GameTest
    public void flightAugmentGrantsAndRevokesOnlyItsOwnFlight(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var cache = new AbilityCache();
        var augment = new FlightAugment(MysticalAgriculture.resource("gametest_flight"), 5);

        require(!player.getAbilities().mayfly, "the test player unexpectedly started with flight");
        augment.onPlayerTick(player, cache);
        require(player.getAbilities().mayfly, "the flight augment did not grant flight");

        cache.remove(augment, player);
        require(!player.getAbilities().mayfly, "removing the augment did not revoke granted flight");

        player.getAbilities().mayfly = true;
        augment.onPlayerTick(player, cache);
        cache.remove(augment, player);
        require(player.getAbilities().mayfly, "the augment revoked flight that it did not grant");
        require(ModAugments.NO_FALL_DAMAGE.onPlayerFall(player, 100.0F),
                "the no-fall augment did not veto server fall damage");
        helper.succeed();
    }

    @GameTest
    public void installedStrengthAugmentReachesItemStackAttributeIteration(GameTestHelper helper) {
        var stack = new ItemStack(ModItems.SUPREMIUM_SWORD);
        AugmentUtils.addAugment(stack, ModAugments.STRENGTH_III, 0);
        var found = new AtomicBoolean();

        stack.forEachModifier(EquipmentSlot.MAINHAND, (_, modifier) -> {
            if (modifier.is(MysticalAgriculture.resource("strength_augment"))) {
                found.set(true);
            }
        });

        require(found.get(), "the ItemStack attribute boundary omitted the installed augment");
        helper.succeed();
    }

    @GameTest
    public void essenceConvertsFarmlandWithoutLosingMoisture(GameTestHelper helper) {
        var position = new BlockPos(1, 1, 1);
        var absolute = helper.absolutePos(position);
        helper.setBlock(position, Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 5));
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(absolute.getX() + 0.5, absolute.getY() + 1, absolute.getZ() + 0.5);
        var stack = new ItemStack(ModItems.INFERIUM_ESSENCE, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        require(stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                "inferium essence did not convert vanilla farmland");

        var converted = helper.getLevel().getBlockState(absolute);
        require(converted.is(ModBlocks.INFERIUM_FARMLAND), "conversion produced the wrong farmland tier");
        require(converted.getValue(FarmlandBlock.MOISTURE) == 5, "conversion lost farmland moisture");
        require(stack.getCount() == 1, "conversion did not consume exactly one essence");
        helper.succeed();
    }

    @GameTest
    public void tillingAoeChangesEachEligibleBlockAndChargesDurability(GameTestHelper helper) {
        var center = new BlockPos(2, 1, 2);
        var east = center.east();
        var absolute = helper.absolutePos(center);
        helper.setBlock(center, Blocks.DIRT);
        helper.setBlock(east, Blocks.DIRT);
        var player = helper.makeMockServerPlayerInLevel();
        player.setPos(absolute.getX() + 0.5, absolute.getY() + 1, absolute.getZ() + 0.5);
        player.setShiftKeyDown(true);
        var stack = new ItemStack(ModItems.SUPREMIUM_HOE);
        AugmentUtils.addAugment(stack, ModAugments.TILLING_AOE_I, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var initialDamage = stack.getDamageValue();

        var hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        require(stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                "the tilling augment rejected an eligible origin");

        require(helper.getLevel().getBlockState(absolute).is(Blocks.FARMLAND),
                "the tilling augment did not convert its origin");
        require(helper.getLevel().getBlockState(helper.absolutePos(east)).is(Blocks.FARMLAND),
                "the tilling augment did not convert an eligible AOE block");
        require(stack.getDamageValue() >= initialDamage + 2,
                "the tilling augment did not charge durability per changed block");
        helper.succeed();
    }

    @GameTest
    public void awakenedSupremiumSetDetectionRequiresEveryArmorPiece(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.AWAKENED_SUPREMIUM_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.AWAKENED_SUPREMIUM_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.AWAKENED_SUPREMIUM_LEGGINGS));

        require(!EssenceChestplateItem.hasAwakenedSupremiumSet(player),
                "an incomplete awakened supremium set activated its bonus");

        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.AWAKENED_SUPREMIUM_BOOTS));
        require(EssenceChestplateItem.hasAwakenedSupremiumSet(player),
                "the complete awakened supremium set did not activate its bonus");
        helper.succeed();
    }

    @GameTest
    public void fakePlayerWateringPolicyFollowsServerConfig(GameTestHelper helper) {
        var wateringCan = new TestWateringCan();
        require(wateringCan.allowsFakePlayerWatering(),
                "the default Fabric server config unexpectedly disabled fake-player watering");
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class TestWateringCan extends WateringCanItem {
        private TestWateringCan() {
            super(Identifier.parse("mysticalagriculture:gametest_watering_can"), 1, 1.0D);
        }

        private boolean allowsFakePlayerWatering() {
            return this.allowFakePlayerWatering();
        }
    }
}
