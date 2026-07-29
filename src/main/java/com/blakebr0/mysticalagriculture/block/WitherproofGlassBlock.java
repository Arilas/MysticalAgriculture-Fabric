package com.blakebr0.mysticalagriculture.block;

import com.blakebr0.cucumber.block.BaseGlassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;

public class WitherproofGlassBlock extends BaseGlassBlock {
    public WitherproofGlassBlock(Identifier id) {
        super(id, p -> p
            .strength(18.0F, 2000.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
        );
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) { }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }
}
