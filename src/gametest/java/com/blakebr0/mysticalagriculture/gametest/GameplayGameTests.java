package com.blakebr0.mysticalagriculture.gametest;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.lib.AbilityCache;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.api.util.ExperienceCapsuleUtils;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.augment.FlightAugment;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.handler.ExperienceCapsuleHandler;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.item.MysticalFertilizerItem;
import com.blakebr0.mysticalagriculture.item.WateringCanItem;
import com.blakebr0.mysticalagriculture.item.armor.EssenceChestplateItem;
import com.blakebr0.mysticalagriculture.lib.ModAugments;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import com.blakebr0.mysticalagriculture.lib.ModMobSoulTypes;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void mergedExperienceOrbConsumesOneUnitAndPreservesUntouchedDenominations(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var capsule = new ItemStack(ModItems.EXPERIENCE_CAPSULE);
        player.setItemInHand(InteractionHand.OFF_HAND, capsule);
        var orb = mergedOrb(helper, 5, 3);

        orb.playerTouch(player);

        require(ExperienceCapsuleUtils.getExperience(capsule) == 5,
                "the capsule did not consume exactly one merged-orb unit");
        require(orb.getValue() == 5,
                "consuming one unit changed the denomination of untouched merged units");
        require(orbCount(helper, orb) == 2,
                "consuming one unit did not preserve the remaining merged-unit count");
        require(!orb.isRemoved(), "consuming one unit discarded untouched merged-orb units");
        require(player.totalExperience == 0, "fully absorbed experience leaked into the player");
        require(ExperienceCapsuleUtils.getExperience(capsule) + orb.getValue() * orbCount(helper, orb) == 15,
                "full capsule absorption did not conserve total experience");
        helper.succeed();
    }

    @GameTest
    public void mergedExperienceOrbPartialCapsuleRoomConservesRemainderAndUntouchedUnits(GameTestHelper helper) {
        var trackedDuringTake = new AtomicBoolean();
        var takeCalls = new AtomicInteger();
        var player = makeTakeObservingPlayer(helper, entity -> {
            takeCalls.incrementAndGet();
            trackedDuringTake.set(
                    entity instanceof ExperienceOrb taken
                            && taken.getValue() == 3
                            && helper.getLevel().getChunkSource().hasEntityWithId(entity.getId())
            );
        });
        var capsule = ExperienceCapsuleUtils.getExperienceCapsule(
                ExperienceCapsuleUtils.MAX_XP_POINTS - 2,
                ModItems.EXPERIENCE_CAPSULE
        );
        player.setItemInHand(InteractionHand.OFF_HAND, capsule);
        var orb = mergedOrb(helper, 5, 3);

        orb.playerTouch(player);

        require(ExperienceCapsuleUtils.getExperience(capsule) == ExperienceCapsuleUtils.MAX_XP_POINTS,
                "the capsule did not fill its two remaining experience points");
        require(orb.getValue() == 5,
                "partial absorption changed the denomination of untouched merged units");
        require(orbCount(helper, orb) == 2,
                "partial absorption did not consume exactly one merged-orb unit");
        require(!orb.isRemoved(), "partial absorption discarded untouched merged-orb units");
        require(player.totalExperience == 3,
                "vanilla did not receive the unabsorbed remainder of the consumed unit");
        require(takeCalls.get() == 1,
                "vanilla processed " + takeCalls.get() + " pickup entities instead of one remainder");
        require(trackedDuringTake.get(),
                "the partial remainder was not tracked during vanilla Player.take");
        require(player.takeXpDelay == 2,
                "vanilla pickup delay was not applied to the partial remainder");
        require(2 + player.totalExperience + orb.getValue() * orbCount(helper, orb) == 15,
                "partial capsule absorption did not conserve total experience");
        helper.succeed();
    }

    @GameTest
    public void singleExperienceOrbPartialCapsuleRoomConservesTrackedRemainder(GameTestHelper helper) {
        var trackedDuringTake = new AtomicBoolean();
        var player = makeTakeObservingPlayer(helper, entity -> trackedDuringTake.set(
                entity instanceof ExperienceOrb taken
                        && taken.getValue() == 3
                        && helper.getLevel().getChunkSource().hasEntityWithId(entity.getId())
        ));
        var capsule = ExperienceCapsuleUtils.getExperienceCapsule(
                ExperienceCapsuleUtils.MAX_XP_POINTS - 2,
                ModItems.EXPERIENCE_CAPSULE
        );
        player.setItemInHand(InteractionHand.OFF_HAND, capsule);
        var orb = mergedOrb(helper, 5, 1);

        orb.playerTouch(player);

        require(ExperienceCapsuleUtils.getExperience(capsule) == ExperienceCapsuleUtils.MAX_XP_POINTS,
                "the capsule did not fill its two remaining experience points");
        require(orb.isRemoved(), "the consumed single-unit orb remained in the level");
        require(player.totalExperience == 3,
                "vanilla did not receive the single orb's unabsorbed remainder");
        require(trackedDuringTake.get(),
                "the single orb's partial remainder was not tracked during vanilla Player.take");
        require(player.takeXpDelay == 2,
                "vanilla pickup delay was not applied to the single orb's remainder");
        require(2 + player.totalExperience == 5,
                "single-unit partial capsule absorption did not conserve total experience");
        helper.succeed();
    }

    @GameTest
    public void partialCapsuleRemainderStillRunsVanillaMending(GameTestHelper helper) {
        var player = makeTakeObservingPlayer(helper, _ -> {
        });
        var capsule = ExperienceCapsuleUtils.getExperienceCapsule(
                ExperienceCapsuleUtils.MAX_XP_POINTS - 2,
                ModItems.EXPERIENCE_CAPSULE
        );
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.setDamageValue(10);
        tool.enchant(
                helper.getLevel().registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.MENDING),
                1
        );
        player.setItemInHand(InteractionHand.OFF_HAND, capsule);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        var orb = mergedOrb(helper, 5, 1);

        orb.playerTouch(player);

        require(ExperienceCapsuleUtils.getExperience(capsule) == ExperienceCapsuleUtils.MAX_XP_POINTS,
                "the capsule did not fill before vanilla mending");
        require(tool.getDamageValue() < 10,
                "the tracked partial remainder did not run vanilla mending");
        require(orb.isRemoved(), "the mending test left the consumed orb in the level");
        require(player.takeXpDelay == 2,
                "the mending remainder did not apply vanilla pickup delay");
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
        var player = makePermissionPlayer(helper, _ -> true);
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

    @GameTest(maxTicks = 20)
    public void registeredFlightLifecycleGrantsAndRevokesEquippedAugment(GameTestHelper helper) {
        var player = makePermissionPlayer(helper, _ -> true);
        var chestplate = new ItemStack(ModItems.SUPREMIUM_CHESTPLATE);
        AugmentUtils.addAugment(chestplate, ModAugments.FLIGHT, 0);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        helper.startSequence()
                .thenExecuteAfter(2, () -> helper.assertTrue(
                        player.getAbilities().mayfly,
                        "registered level tick did not grant equipped flight"
                ))
                .thenExecute(() -> player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY))
                .thenExecuteAfter(2, () -> helper.assertTrue(
                        !player.getAbilities().mayfly,
                        "registered level tick did not revoke removed flight"
                ))
                .thenSucceed();
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
        var player = makePermissionPlayer(helper, _ -> true);
        player.setPos(absolute.getX() + 0.5, absolute.getY() + 1, absolute.getZ() + 0.5);
        player.setShiftKeyDown(true);
        player.setPose(Pose.CROUCHING);
        var stack = new ItemStack(ModItems.PRUDENTIUM_HOE);
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
    public void miningAoeUsesOriginalMinedStateAfterOriginRemoval(GameTestHelper helper) {
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        var neighbor = origin.east();
        var originalState = Blocks.STONE.defaultBlockState();
        helper.getLevel().setBlock(origin.below(), Blocks.BEDROCK.defaultBlockState(), 3);
        helper.getLevel().setBlock(origin, originalState, 3);
        helper.getLevel().setBlock(neighbor, originalState, 3);
        var player = makePermissionPlayer(helper, _ -> true);
        player.setPos(origin.getX() + 0.5D, origin.getY() + 2.0D, origin.getZ() + 0.5D);
        player.setXRot(90.0F);
        var stack = new ItemStack(ModItems.PRUDENTIUM_PICKAXE);
        AugmentUtils.addAugment(stack, ModAugments.MINING_AOE_I, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.getLevel().removeBlock(origin, false);
        stack.mineBlock(helper.getLevel(), originalState, origin, player);

        require(helper.getLevel().getBlockState(neighbor).isAir(),
                "mining AOE reread the removed origin as air and left its neighbor intact");
        require(stack.getDamageValue() == 2,
                "mining the origin and one AOE neighbor charged "
                        + stack.getDamageValue() + " durability instead of two");
        helper.succeed();
    }

    @GameTest
    public void miningAoeLeavesDeniedNeighborAndBreaksAllowedNeighbor(GameTestHelper helper) {
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        var allowed = origin.east();
        var denied = origin.west();
        var originalState = Blocks.STONE.defaultBlockState();
        helper.getLevel().setBlock(origin.below(), Blocks.BEDROCK.defaultBlockState(), 3);
        helper.getLevel().setBlock(origin, originalState, 3);
        helper.getLevel().setBlock(allowed, originalState, 3);
        helper.getLevel().setBlock(denied, originalState, 3);
        var player = makePermissionPlayer(helper, position -> !position.equals(denied));
        player.setPos(origin.getX() + 0.5D, origin.getY() + 2.0D, origin.getZ() + 0.5D);
        player.setXRot(90.0F);
        var stack = new ItemStack(ModItems.PRUDENTIUM_PICKAXE);
        AugmentUtils.addAugment(stack, ModAugments.MINING_AOE_I, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.getLevel().removeBlock(origin, false);
        stack.mineBlock(helper.getLevel(), originalState, origin, player);

        require(helper.getLevel().getBlockState(allowed).isAir(),
                "mining AOE did not break the allowed neighbor");
        require(helper.getLevel().getBlockState(denied).is(Blocks.STONE),
                "mining AOE changed a neighbor denied by mayUseItemAt");
        require(stack.getDamageValue() == 2,
                "mining AOE charged " + stack.getDamageValue()
                        + " durability instead of two for origin plus allowed neighbor");
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

    @GameTest(maxTicks = 40)
    public void awakenedSupremiumInventoryTickActuallyGrowsNearbyCrops(GameTestHelper helper) {
        var player = makePermissionPlayer(helper, _ -> true);
        var chestplate = new ItemStack(ModItems.AWAKENED_SUPREMIUM_CHESTPLATE);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.AWAKENED_SUPREMIUM_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.AWAKENED_SUPREMIUM_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.AWAKENED_SUPREMIUM_BOOTS));
        player.setPos(
                helper.absolutePos(new BlockPos(3, 2, 3)).getX() + 0.5D,
                helper.absolutePos(new BlockPos(3, 2, 3)).getY(),
                helper.absolutePos(new BlockPos(3, 2, 3)).getZ() + 0.5D
        );

        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.FARMLAND);
                helper.setBlock(new BlockPos(x, 1, z), Blocks.WHEAT);
            }
        }

        Runnable runGrowthTicks = () -> {
            for (int i = 0; i < 100; i++) {
                chestplate.inventoryTick(helper.getLevel(), player, EquipmentSlot.CHEST);
            }

            var grew = false;
            for (int x = 1; x <= 5 && !grew; x++) {
                for (int z = 1; z <= 5; z++) {
                    var state = helper.getBlockState(new BlockPos(x, 1, z));
                    if (state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) > 0) {
                        grew = true;
                        break;
                    }
                }
            }
            require(grew, "actual awakened chestplate inventory ticks did not grow any nearby crop");
            helper.succeed();
        };

        var remainder = helper.getLevel().getGameTime() % 20L;
        if (remainder == 0L) {
            runGrowthTicks.run();
        } else {
            helper.runAfterDelay(20L - remainder, runGrowthTicks);
        }
    }

    @GameTest
    public void effectiveFarmlandConfigGatesActualMysticalCropGrowth(GameTestHelper helper) {
        var ineffective = helper.absolutePos(new BlockPos(1, 1, 1));
        var effective = helper.absolutePos(new BlockPos(3, 1, 1));
        var crop = ModCrops.NATURE.getCropBlock();
        helper.getLevel().setBlock(ineffective.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                effective.below(),
                ModBlocks.PRUDENTIUM_FARMLAND.defaultBlockState(),
                3
        );
        helper.getLevel().setBlock(ineffective, crop.defaultBlockState(), 3);
        helper.getLevel().setBlock(effective, crop.defaultBlockState(), 3);

        setConfigValue(ModConfigs.REQUIRES_EFFECTIVE_FARMLAND, true);
        try {
            crop.performBonemeal(
                    helper.getLevel(),
                    helper.getLevel().getRandom(),
                    ineffective,
                    helper.getLevel().getBlockState(ineffective)
            );
            crop.performBonemeal(
                    helper.getLevel(),
                    helper.getLevel().getRandom(),
                    effective,
                    helper.getLevel().getBlockState(effective)
            );
        } finally {
            setConfigValue(ModConfigs.REQUIRES_EFFECTIVE_FARMLAND, false);
        }

        require(helper.getLevel().getBlockState(ineffective).getValue(CropBlock.AGE) == 0,
                "a tier-two crop grew on ineffective vanilla farmland");
        require(helper.getLevel().getBlockState(effective).getValue(CropBlock.AGE) > 0,
                "a tier-two crop did not grow on prudentium farmland");
        helper.succeed();
    }

    @GameTest
    public void registeredMobSoulCallbackFillsJarFromActualDeath(GameTestHelper helper) {
        var player = makePermissionPlayer(helper, _ -> true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SOULIUM_DAGGER));
        player.getInventory().add(new ItemStack(ModItems.SOUL_JAR));
        var jar = player.getInventory().getNonEquipmentItems()
                .stream()
                .filter(stack -> stack.is(ModItems.SOUL_JAR))
                .findFirst()
                .orElseThrow();
        var zombie = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 1, 2));
        zombie.setHealth(1.0F);

        require(zombie.hurtServer(
                        helper.getLevel(),
                        helper.getLevel().damageSources().playerAttack(player),
                        20.0F
                ),
                "the zombie rejected lethal player damage");
        require(MobSoulUtils.getType(jar) == ModMobSoulTypes.ZOMBIE,
                "registered death callback put the wrong soul type in the jar");
        require(MobSoulUtils.getSouls(jar) == 1.0D,
                "registered death callback did not siphon exactly one zombie soul");
        helper.succeed();
    }

    @GameTest
    public void registeredMobDropCallbackSpawnsTieredWitherEssence(GameTestHelper helper) {
        var player = makePermissionPlayer(helper, _ -> true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.PRUDENTIUM_SWORD));
        var wither = helper.spawn(EntityTypes.WITHER, new BlockPos(2, 2, 2));
        wither.setInvulnerableTicks(0);
        wither.setHealth(1.0F);

        require(wither.hurtServer(
                        helper.getLevel(),
                        helper.getLevel().damageSources().playerAttack(player),
                        20.0F
                ),
                "the wither rejected lethal player damage");

        var drops = helper.getLevel().getEntities(
                EntityTypeTest.forClass(ItemEntity.class),
                entity -> entity.getItem().is(ModItems.PRUDENTIUM_ESSENCE)
        );
        require(drops.size() == 1,
                "registered death callback spawned " + drops.size()
                        + " prudentium essence entities instead of one");
        require(drops.getFirst().getItem().getCount() >= 1
                        && drops.getFirst().getItem().getCount() <= 3,
                "tiered wither essence count was outside the configured 1-3 range");
        helper.succeed();
    }

    @GameTest
    public void canonicalFakePlayerWateringHonorsConfigThrottleAndDeniedNeighbor(GameTestHelper helper) {
        var origin = helper.absolutePos(new BlockPos(2, 1, 2));
        var allowed = origin.east();
        var denied = origin.west();
        var dry = Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 0);
        helper.getLevel().setBlock(origin, dry, 3);
        helper.getLevel().setBlock(allowed, dry, 3);
        helper.getLevel().setBlock(
                denied,
                ModBlocks.INFERIUM_FARMLAND.defaultBlockState()
                        .setValue(FarmlandBlock.MOISTURE, 0),
                3
        );
        var player = FakePlayer.get(helper.getLevel());
        player.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
        var stack = new ItemStack(ModItems.WATERING_CAN);
        WateringCanItem.setFilled(stack, true);
        stack.set(
                DataComponents.CAN_PLACE_ON,
                new AdventureModePredicate(List.of(
                        BlockPredicate.Builder.block()
                                .of(
                                        helper.getLevel().registryAccess()
                                                .lookupOrThrow(Registries.BLOCK),
                                        Blocks.FARMLAND
                                )
                                .build()
                ))
        );
        var hit = new BlockHitResult(Vec3.atCenterOf(origin), Direction.UP, origin, false);
        var previousStack = player.getMainHandItem();
        var previousMayBuild = player.getAbilities().mayBuild;
        var previousConfig = ModConfigs.FAKE_PLAYER_WATERING.get();
        var cooldowns = player.getCooldowns();
        var cooldownGroup = cooldowns.getCooldownGroup(stack);

        try {
            player.getAbilities().mayBuild = false;
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            cooldowns.removeCooldown(cooldownGroup);

            setConfigValue(ModConfigs.FAKE_PLAYER_WATERING, false);
            stack.useOn(new net.minecraft.world.item.context.UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    hit
            ));
            require(helper.getLevel().getBlockState(origin).getValue(FarmlandBlock.MOISTURE) == 0,
                    "the canonical Fabric fake player bypassed disabled watering");
            require(!cooldowns.isOnCooldown(stack),
                    "denied fake-player watering incorrectly started a cooldown");

            setConfigValue(ModConfigs.FAKE_PLAYER_WATERING, true);
            stack.useOn(new net.minecraft.world.item.context.UseOnContext(
                    player,
                    InteractionHand.MAIN_HAND,
                    hit
            ));

            require(helper.getLevel().getBlockState(origin).getValue(FarmlandBlock.MOISTURE) == 7,
                    "the canonical Fabric fake player did not water the clicked farmland");
            require(helper.getLevel().getBlockState(allowed).getValue(FarmlandBlock.MOISTURE) == 7,
                    "the canonical Fabric fake player did not water an allowed neighbor");
            require(helper.getLevel().getBlockState(denied).getValue(FarmlandBlock.MOISTURE) == 0,
                    "the canonical Fabric fake player watered a denied neighbor");
            for (int tick = 0; tick < 9; tick++) {
                cooldowns.tick();
            }
            require(cooldowns.isOnCooldown(stack),
                    "canonical fake-player watering cooldown ended before tick 10");
            cooldowns.tick();
            require(!cooldowns.isOnCooldown(stack),
                    "canonical fake-player watering cooldown lasted beyond tick 10");
        } finally {
            cooldowns.removeCooldown(cooldownGroup);
            player.setItemInHand(InteractionHand.MAIN_HAND, previousStack);
            player.getAbilities().mayBuild = previousMayBuild;
            setConfigValue(ModConfigs.FAKE_PLAYER_WATERING, previousConfig);
        }
        require(ModConfigs.FAKE_PLAYER_WATERING.get().equals(previousConfig),
                "fake-player watering test did not restore the global config value");
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static ExperienceOrb mergedOrb(GameTestHelper helper, int denomination, int count) {
        var level = helper.getLevel();
        var position = helper.absolutePos(new BlockPos(1, 2, 1));
        var orb = new ExperienceOrb(level, Vec3.atCenterOf(position), Vec3.ZERO, denomination);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        orb.saveWithoutId(output);
        var tag = output.buildResult();
        tag.putInt("Count", count);
        orb.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        level.addFreshEntity(orb);
        return orb;
    }

    private static int orbCount(GameTestHelper helper, ExperienceOrb orb) {
        var output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING,
                helper.getLevel().registryAccess()
        );
        orb.saveWithoutId(output);
        return output.buildResult().getIntOr("Count", 0);
    }

    private static ServerPlayer makePermissionPlayer(
            GameTestHelper helper,
            Predicate<BlockPos> mayUse
    ) {
        return makePlayer(helper, mayUse, _ -> {
        });
    }

    private static ServerPlayer makeTakeObservingPlayer(
            GameTestHelper helper,
            Consumer<net.minecraft.world.entity.Entity> onTake
    ) {
        return makePlayer(helper, _ -> true, onTake);
    }

    private static ServerPlayer makePlayer(
            GameTestHelper helper,
            Predicate<BlockPos> mayUse,
            Consumer<net.minecraft.world.entity.Entity> onTake
    ) {
        var cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "task5-permission-player"),
                false
        );
        var level = helper.getLevel();
        var player = new ServerPlayer(
                level.getServer(),
                level,
                cookie.gameProfile(),
                cookie.clientInformation()
        ) {
            @Override
            public net.minecraft.world.level.GameType gameMode() {
                return net.minecraft.world.level.GameType.SURVIVAL;
            }

            @Override
            public boolean mayUseItemAt(BlockPos pos, Direction facing, ItemStack stack) {
                return mayUse.test(pos.relative(facing.getOpposite()));
            }

            @Override
            public void take(net.minecraft.world.entity.Entity entity, int count) {
                onTake.accept(entity);
                super.take(entity, count);
            }
        };
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        var abilities = player.getAbilities();
        abilities.invulnerable = false;
        abilities.flying = false;
        abilities.mayfly = false;
        abilities.instabuild = false;
        abilities.mayBuild = true;
        return player;
    }

    private static <T> void setConfigValue(ModConfigs.ConfigValue<T> config, T value) {
        try {
            var field = ModConfigs.ConfigValue.class.getDeclaredField("value");
            field.setAccessible(true);
            field.set(config, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("could not isolate the runtime config boundary", e);
        }
    }

}
