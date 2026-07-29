package com.blakebr0.mysticalagriculture.client;

import com.blakebr0.mysticalagriculture.client.tesr.renderer.AwakeningAltarRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.AwakeningPedestalRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.EnchanterRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.EssenceVesselRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.InfusionAltarRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.InfusionPedestalRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.SouliumSpawnerRenderer;
import com.blakebr0.mysticalagriculture.client.tesr.renderer.TinkeringTableRenderer;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class ModTESRs {
    private ModTESRs() {
    }

    public static void register() {
        BlockEntityRenderers.register(ModTileEntities.INFUSION_PEDESTAL, InfusionPedestalRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.INFUSION_ALTAR, InfusionAltarRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.TINKERING_TABLE, TinkeringTableRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.ENCHANTER, EnchanterRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.AWAKENING_PEDESTAL, AwakeningPedestalRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.AWAKENING_ALTAR, AwakeningAltarRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.ESSENCE_VESSEL, EssenceVesselRenderer::new);
        BlockEntityRenderers.register(ModTileEntities.SOULIUM_SPAWNER, SouliumSpawnerRenderer::new);
    }
}
