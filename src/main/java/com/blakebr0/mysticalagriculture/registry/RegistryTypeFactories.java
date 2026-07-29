package com.blakebr0.mysticalagriculture.registry;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public final class RegistryTypeFactories {
    public static <T extends BlockEntity> BlockEntityType<T> blockEntity(
            BlockEntityType.BlockEntitySupplier<T> factory,
            Block... blocks
    ) {
        return new BlockEntityType<>(factory, Set.of(blocks));
    }

    public static <T extends AbstractContainerMenu, D> ExtendedMenuType<T, D> extendedMenu(
            ExtendedMenuType.ExtendedFactory<T, D> factory,
            StreamCodec<? super RegistryFriendlyByteBuf, D> codec
    ) {
        return new ExtendedMenuType<>(factory, codec);
    }

    private RegistryTypeFactories() {
    }
}
