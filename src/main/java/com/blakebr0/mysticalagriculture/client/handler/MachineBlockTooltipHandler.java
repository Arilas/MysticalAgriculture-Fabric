package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.cucumber.lib.Tooltips;
import com.blakebr0.cucumber.util.Formatting;
import com.blakebr0.mysticalagriculture.block.EssenceFurnaceBlock;
import com.blakebr0.mysticalagriculture.block.HarvesterBlock;
import com.blakebr0.mysticalagriculture.block.OreInfuserBlock;
import com.blakebr0.mysticalagriculture.block.ReprocessorBlock;
import com.blakebr0.mysticalagriculture.block.SoulExtractorBlock;
import com.blakebr0.mysticalagriculture.block.SouliumSpawnerBlock;
import com.blakebr0.mysticalagriculture.client.util.ClientInputUtil;
import com.blakebr0.mysticalagriculture.tileentity.EssenceFurnaceTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.HarvesterTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.OreInfuserTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.ReprocessorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SoulExtractorTileEntity;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class MachineBlockTooltipHandler {
    private MachineBlockTooltipHandler() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, _, _, lines) -> {
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return;
            }

            var block = blockItem.getBlock();
            if (!isMachine(block)) {
                return;
            }
            if (!ClientInputUtil.isShiftDown()) {
                lines.add(Tooltips.HOLD_SHIFT_FOR_INFO.toComponent());
                return;
            }

            if (block instanceof EssenceFurnaceBlock) {
                addStandard(lines, EssenceFurnaceTileEntity.OPERATION_TIME,
                        EssenceFurnaceTileEntity.FUEL_USAGE, EssenceFurnaceTileEntity.FUEL_CAPACITY);
            } else if (block instanceof ReprocessorBlock) {
                addStandard(lines, ReprocessorTileEntity.OPERATION_TIME,
                        ReprocessorTileEntity.FUEL_USAGE, ReprocessorTileEntity.FUEL_CAPACITY);
            } else if (block instanceof SoulExtractorBlock) {
                addStandard(lines, SoulExtractorTileEntity.OPERATION_TIME,
                        SoulExtractorTileEntity.FUEL_USAGE, SoulExtractorTileEntity.FUEL_CAPACITY);
            } else if (block instanceof OreInfuserBlock) {
                addStandard(lines, OreInfuserTileEntity.OPERATION_TIME,
                        OreInfuserTileEntity.FUEL_USAGE, OreInfuserTileEntity.FUEL_CAPACITY);
            } else if (block instanceof HarvesterBlock) {
                var range = HarvesterTileEntity.BASE_RANGE * 2 + 1;
                lines.add(tooltip("machine_area",
                        Component.literal(range + "x" + range).withStyle(ChatFormatting.WHITE)));
                lines.add(tooltip("machine_speed", number(HarvesterTileEntity.OPERATION_TIME)));
                lines.add(tooltip("machine_scan_fuel_usage", number(HarvesterTileEntity.SCAN_FUEL_USAGE)));
                lines.add(tooltip("machine_fuel_usage", number(HarvesterTileEntity.FUEL_USAGE)));
                lines.add(tooltip("machine_fuel_capacity", number(HarvesterTileEntity.FUEL_CAPACITY)));
            } else if (block instanceof SouliumSpawnerBlock) {
                addStandard(lines, SouliumSpawnerTileEntity.OPERATION_TIME,
                        SouliumSpawnerTileEntity.FUEL_USAGE, SouliumSpawnerTileEntity.FUEL_CAPACITY);
                lines.add(tooltip("machine_spawn_radius", number(SouliumSpawnerTileEntity.SPAWN_RADIUS)));
            }
        });
    }

    private static boolean isMachine(Object block) {
        return block instanceof EssenceFurnaceBlock
                || block instanceof ReprocessorBlock
                || block instanceof SoulExtractorBlock
                || block instanceof HarvesterBlock
                || block instanceof OreInfuserBlock
                || block instanceof SouliumSpawnerBlock;
    }

    private static void addStandard(List<Component> lines, int speed, int fuelRate, int fuelCapacity) {
        lines.add(tooltip("machine_speed", number(speed)));
        lines.add(tooltip("machine_fuel_rate", number(fuelRate)));
        lines.add(tooltip("machine_fuel_capacity", number(fuelCapacity)));
    }

    private static Component number(int value) {
        return Formatting.number(value).withStyle(ChatFormatting.WHITE);
    }

    private static Component tooltip(String name, Object value) {
        return Component.translatable("tooltip.mysticalagriculture." + name, value)
                .withStyle(ChatFormatting.GRAY);
    }
}
