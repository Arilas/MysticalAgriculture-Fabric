package com.blakebr0.mysticalagriculture.compat;

import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.AwakeningPedestalTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.EssenceVesselTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.InfusionAltarTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.InfusionPedestalTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JadeStorageProviderTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        Task7TestBootstrap.ensureInitialized();
    }

    @Test
    void registersNativeItemAndEnergySnapshotsForEveryFabricStorageOwner() {
        var registrations = registrations();

        assertEquals(List.of(
                "AwakeningAltarTileEntity",
                "AwakeningPedestalTileEntity",
                "EssenceVesselTileEntity",
                "EssenceFurnaceTileEntity",
                "HarvesterTileEntity",
                "InfusionAltarTileEntity",
                "InfusionPedestalTileEntity",
                "ReprocessorTileEntity",
                "SoulExtractorTileEntity",
                "SouliumSpawnerTileEntity",
                "OreInfuserTileEntity"
        ), registrations.item().stream().map(ProviderRegistration::targetName).toList());
        assertEquals(List.of(
                "EssenceFurnaceTileEntity",
                "HarvesterTileEntity",
                "ReprocessorTileEntity",
                "SoulExtractorTileEntity",
                "SouliumSpawnerTileEntity",
                "OreInfuserTileEntity"
        ), registrations.energy().stream().map(ProviderRegistration::targetName).toList());
    }

    @Test
    void nativeItemProvidersExposeIndependentProductionInventorySnapshots() {
        var registrations = registrations();
        var spawner = new SouliumSpawnerTileEntity(BlockPos.ZERO, ModBlocks.SOULIUM_SPAWNER.defaultBlockState());
        var altar = new InfusionAltarTileEntity(BlockPos.ZERO, ModBlocks.INFUSION_ALTAR.defaultBlockState());
        var pedestal = new InfusionPedestalTileEntity(BlockPos.ZERO, ModBlocks.INFUSION_PEDESTAL.defaultBlockState());
        var vessel = new EssenceVesselTileEntity(BlockPos.ZERO, ModBlocks.ESSENCE_VESSEL.defaultBlockState());

        spawner.getInventory().set(0, ItemVariant.of(Items.ROTTEN_FLESH), 512);
        altar.getInventory().set(0, ItemVariant.of(Items.DIAMOND), 7);
        pedestal.getInventory().set(0, ItemVariant.of(Items.EMERALD), 3);
        vessel.getInventory().set(0, ItemVariant.of(Items.REDSTONE), 40);

        var spawnerViews = itemViews(registrations, SouliumSpawnerTileEntity.class, spawner);
        var altarViews = itemViews(registrations, InfusionAltarTileEntity.class, altar);
        var pedestalViews = itemViews(registrations, InfusionPedestalTileEntity.class, pedestal);
        var vesselViews = itemViews(registrations, EssenceVesselTileEntity.class, vessel);

        assertEquals(512, spawnerViews.getFirst().getCount());
        assertEquals(7, altarViews.getFirst().getCount());
        assertEquals(3, pedestalViews.getFirst().getCount());
        assertEquals(40L, vesselViews.getFirst().getCount());

        spawnerViews.getFirst().setCount(1);
        altarViews.getFirst().shrink(6);
        assertEquals(512L, spawner.getInventory().getAmountAsLong(0));
        assertEquals(7L, altar.getInventory().getAmountAsLong(0));
    }

    @Test
    void nativeEnergyProviderSnapshotsPoweredMachineWithoutExposingStorage() {
        var registrations = registrations();
        var furnace = new EssenceFurnaceTileEntity(BlockPos.ZERO, ModBlocks.FURNACE.defaultBlockState());
        furnace.getInventory().set(0, ItemVariant.of(Items.RAW_IRON), 9);
        furnace.getEnergy().set(12_345);

        var itemViews = itemViews(registrations, EssenceFurnaceTileEntity.class, furnace);
        var energyViews = energyViews(registrations, EssenceFurnaceTileEntity.class, furnace);

        assertEquals(9, itemViews.getFirst().getCount());
        assertEquals(List.of(new EnergyView.Data(12_345, 80_000)), energyViews);

        furnace.getEnergy().set(1);
        assertEquals(12_345, energyViews.getFirst().current());
    }

    @Test
    void nativeProvidersReturnNoGroupsForEmptyOrWrongTargets() {
        var registrations = registrations();
        var emptyPedestal = new AwakeningPedestalTileEntity(
                BlockPos.ZERO,
                ModBlocks.AWAKENING_PEDESTAL.defaultBlockState()
        );
        var wrongTarget = new EssenceVesselTileEntity(
                BlockPos.ZERO,
                ModBlocks.ESSENCE_VESSEL.defaultBlockState()
        );

        assertTrue(itemViews(
                registrations,
                AwakeningPedestalTileEntity.class,
                emptyPedestal
        ).isEmpty());

        var spawnerProvider = find(registrations.item(), SouliumSpawnerTileEntity.class).provider();
        assertTrue(flatten(spawnerProvider.getGroups(accessor(wrongTarget))).isEmpty());

        var furnaceEnergy = find(registrations.energy(), EssenceFurnaceTileEntity.class).provider();
        assertTrue(flatten(furnaceEnergy.getGroups(accessor(wrongTarget))).isEmpty());
    }

    private static Registrations registrations() {
        var items = new ArrayList<ProviderRegistration<ItemStack>>();
        var energy = new ArrayList<ProviderRegistration<EnergyView.Data>>();
        var registration = proxy(IWailaCommonRegistration.class, (method, args) -> {
            if (method.getName().equals("registerItemStorage")) {
                items.add(new ProviderRegistration<>(
                        castProvider(args[0]),
                        (Class<? extends BlockEntity>) args[1]
                ));
            } else if (method.getName().equals("registerEnergyStorage")) {
                energy.add(new ProviderRegistration<>(
                        castProvider(args[0]),
                        (Class<? extends BlockEntity>) args[1]
                ));
            }
            return null;
        });

        new JadeCompat().register(registration);
        return new Registrations(items, energy);
    }

    private static List<ItemStack> itemViews(
            Registrations registrations,
            Class<? extends BlockEntity> target,
            BlockEntity blockEntity
    ) {
        return flatten(find(registrations.item(), target).provider().getGroups(accessor(blockEntity)));
    }

    private static List<EnergyView.Data> energyViews(
            Registrations registrations,
            Class<? extends BlockEntity> target,
            BlockEntity blockEntity
    ) {
        return flatten(find(registrations.energy(), target).provider().getGroups(accessor(blockEntity)));
    }

    private static <T> ProviderRegistration<T> find(
            List<ProviderRegistration<T>> registrations,
            Class<? extends BlockEntity> target
    ) {
        return registrations.stream()
                .filter(registration -> registration.target() == target)
                .findFirst()
                .orElseThrow();
    }

    private static <T> List<T> flatten(List<ViewGroup<T>> groups) {
        return groups.stream().flatMap(group -> group.views.stream()).toList();
    }

    private static BlockAccessor accessor(BlockEntity blockEntity) {
        return proxy(BlockAccessor.class, (method, _) -> switch (method.getName()) {
            case "getBlockEntity", "getTarget" -> blockEntity;
            case "getBlockState" -> blockEntity.getBlockState();
            case "getBlock" -> blockEntity.getBlockState().getBlock();
            case "getPosition" -> blockEntity.getBlockPos();
            case "getAccessorType" -> BlockAccessor.class;
            default -> null;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> IServerExtensionProvider<T> castProvider(Object provider) {
        return (IServerExtensionProvider<T>) provider;
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (_, method, args) -> {
            var actualArgs = args == null ? new Object[0] : args;
            var result = invocation.accept(method, actualArgs);
            return result != null ? result : defaultValue(method.getReturnType());
        }));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class)
            return null;
        if (!type.isPrimitive())
            return null;
        if (type == boolean.class)
            return false;
        if (type == char.class)
            return '\0';
        if (type == byte.class)
            return (byte) 0;
        if (type == short.class)
            return (short) 0;
        if (type == int.class)
            return 0;
        if (type == long.class)
            return 0L;
        if (type == float.class)
            return 0F;
        if (type == double.class)
            return 0D;
        throw new IllegalArgumentException("Unsupported primitive " + type);
    }

    private record ProviderRegistration<T>(
            IServerExtensionProvider<T> provider,
            Class<? extends BlockEntity> target
    ) {
        String targetName() {
            return this.target.getSimpleName();
        }
    }

    private record Registrations(
            List<ProviderRegistration<ItemStack>> item,
            List<ProviderRegistration<EnergyView.Data>> energy
    ) {
    }

    @FunctionalInterface
    private interface Invocation {
        Object accept(java.lang.reflect.Method method, Object[] args);
    }
}
