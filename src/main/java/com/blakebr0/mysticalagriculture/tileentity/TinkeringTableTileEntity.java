package com.blakebr0.mysticalagriculture.tileentity;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.mysticalagriculture.api.tinkering.IAugmentProvider;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.container.TinkeringTableContainer;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.lib.ModCrops;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class TinkeringTableTileEntity extends BaseInventoryTileEntity implements ExtendedMenuProvider<BlockPos> {
    private final CItemStacksHandler inventory;

    public TinkeringTableTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.TINKERING_TABLE, pos, state);
        this.inventory = createInventoryHandler((_, _) -> this.setChangedAndDispatch());
    }

    @Override
    public CItemStacksHandler getInventory() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mysticalagriculture.tinkering_table");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos().immutable();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new TinkeringTableContainer(windowId, playerInventory, this.inventory, this.getBlockPos());
    }

    public static CItemStacksHandler createInventoryHandler() {
        return createInventoryHandler(null);
    }

    public static CItemStacksHandler createInventoryHandler(OnContentsChangedFunction onContentsChanged) {
        return CItemStacksHandler.create(7, onContentsChanged, builder -> {
            builder.setDefaultSlotLimit(1);
            builder.setCanInsert((slot, stack) -> {
                var item = stack.getItem();
                return switch (slot) {
                    case 0 -> item instanceof ITinkerable;
                    case 1, 2 -> item instanceof IAugmentProvider;
                    case 3 -> item == ModCrops.AIR.getEssenceItem();
                    case 4 -> item == ModCrops.EARTH.getEssenceItem();
                    case 5 -> item == ModCrops.WATER.getEssenceItem();
                    case 6 -> item == ModCrops.FIRE.getEssenceItem();
                    default -> true;
                };
            });
        });
    }
}
