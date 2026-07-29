package com.blakebr0.mysticalagriculture.tileentity;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AwakeningPedestalTileEntity extends BaseInventoryTileEntity {
    private final CItemStacksHandler inventory;

    public AwakeningPedestalTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.AWAKENING_PEDESTAL, pos, state);
        this.inventory = CItemStacksHandler.create(1, (_, _) -> this.setChangedAndDispatch(), handler -> {
            handler.setDefaultSlotLimit(1);
        });
    }

    @Override
    public CItemStacksHandler getInventory() {
        return this.inventory;
    }
}
