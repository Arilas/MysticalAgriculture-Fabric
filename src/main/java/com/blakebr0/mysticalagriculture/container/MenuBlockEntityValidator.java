package com.blakebr0.mysticalagriculture.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MenuBlockEntityValidator {
    private MenuBlockEntityValidator() {
    }

    public static <T extends BlockEntity> T require(Inventory inventory, BlockPos pos, Class<T> expectedType) {
        var player = inventory.player;
        var level = player.level();

        if (!level.hasChunkAt(pos)) {
            throw new IllegalArgumentException("Menu block position is not loaded: " + pos);
        }

        if (!player.isWithinBlockInteractionRange(pos, 0.0)) {
            throw new IllegalArgumentException("Menu block position is outside interaction range: " + pos);
        }

        var blockEntity = level.getBlockEntity(pos);
        if (!expectedType.isInstance(blockEntity)) {
            var actualType = blockEntity == null ? "none" : blockEntity.getClass().getName();
            throw new IllegalArgumentException("Expected " + expectedType.getName() + " at " + pos + ", found " + actualType);
        }

        return expectedType.cast(blockEntity);
    }
}
