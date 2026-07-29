package com.blakebr0.mysticalagriculture.tileentity;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class InfusionPedestalTileEntity extends BaseInventoryTileEntity {
    private final CItemStacksHandler inventory;

    public InfusionPedestalTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.INFUSION_PEDESTAL, pos, state);
        this.inventory = createInventoryHandler((_, _) -> this.setChangedAndDispatch());
    }

    @Override
    public CItemStacksHandler getInventory() {
        return this.inventory;
    }

    public static CItemStacksHandler createInventoryHandler(OnContentsChangedFunction onContentsChanged) {
        return CItemStacksHandler.create(1, onContentsChanged, builder -> {
            builder.setDefaultSlotLimit(1);
        });
    }
}
