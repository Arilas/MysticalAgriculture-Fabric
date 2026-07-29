package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.item.AugmentItem;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.List;

public final class AugmentTooltipHandler {
    @SubscribeEvent
    public void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        var stack = event.getItemStack();
        if (stack.getItem() instanceof AugmentItem item) {
            var tooltip = event.getTooltipElements();
            var stacks = getDisplayItemStacks(item.getAugment());

            tooltip.add(1, Either.right(new AugmentToolTypesComponent((10 * stacks.size()) - 10, 12, stacks)));
        }
    }

    private static List<ItemStack> getDisplayItemStacks(Augment augment) {
        var types = augment.getAugmentTypes();

        if (types.contains(AugmentType.TOOL)) {
            return List.of(
                    new ItemStack(Items.DIAMOND_PICKAXE),
                    new ItemStack(Items.DIAMOND_SHOVEL),
                    new ItemStack(Items.DIAMOND_AXE),
                    new ItemStack(Items.DIAMOND_HOE),
                    new ItemStack(Items.SHEARS),
                    new ItemStack(Items.FISHING_ROD),
                    new ItemStack(ModItems.DIAMOND_SICKLE),
                    new ItemStack(ModItems.DIAMOND_SCYTHE)
            );
        }

        if (types.contains(AugmentType.WEAPON)) {
            return List.of(
                    new ItemStack(Items.DIAMOND_SWORD),
                    new ItemStack(Items.BOW),
                    new ItemStack(Items.CROSSBOW),
                    new ItemStack(Items.DIAMOND_SPEAR),
                    new ItemStack(ModItems.DIAMOND_SICKLE),
                    new ItemStack(ModItems.DIAMOND_SCYTHE)
            );
        }

        if (types.contains(AugmentType.ARMOR)) {
            return List.of(
                    new ItemStack(Items.DIAMOND_HELMET),
                    new ItemStack(Items.DIAMOND_CHESTPLATE),
                    new ItemStack(Items.DIAMOND_LEGGINGS),
                    new ItemStack(Items.DIAMOND_BOOTS)
            );
        }

        return types.stream()
                .map(AugmentTooltipHandler::getDisplayItemStackForAugmentType)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static ItemStack getDisplayItemStackForAugmentType(AugmentType type) {
        var item = switch (type) {
            case TOOL, WEAPON, ARMOR, STAFF -> null;
            case SWORD -> Items.DIAMOND_SWORD;
            case PICKAXE -> Items.DIAMOND_PICKAXE;
            case SHOVEL -> Items.DIAMOND_SHOVEL;
            case AXE -> Items.DIAMOND_AXE;
            case HOE -> Items.DIAMOND_HOE;
            case BOW -> Items.BOW;
            case CROSSBOW -> Items.CROSSBOW;
            case SPEAR -> Items.DIAMOND_SPEAR;
            case SHEARS -> Items.SHEARS;
            case FISHING_ROD -> Items.FISHING_ROD;
            case SICKLE -> ModItems.DIAMOND_SICKLE;
            case SCYTHE -> ModItems.DIAMOND_SCYTHE;
            case HELMET -> Items.DIAMOND_HELMET;
            case CHESTPLATE -> Items.DIAMOND_CHESTPLATE;
            case LEGGINGS -> Items.DIAMOND_LEGGINGS;
            case BOOTS -> Items.DIAMOND_BOOTS;
        };

        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public record AugmentToolTypesComponent(int width, int height, List<ItemStack> stacks) implements ClientTooltipComponent, TooltipComponent {
        @Override
        public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor gfx) {
            var matrix = gfx.pose();

            matrix.pushMatrix();
            matrix.translate(x, y);
            matrix.scale(0.5f, 0.5f);

            int drawn = 0;

            for (var stack : stacks) {
                gfx.item(stack, (drawn % 10) * 18, 0);

                drawn++;
            }

            matrix.popMatrix();
        }

        @Override
        public int getHeight(Font font) {
            return this.height;
        }

        @Override
        public int getWidth(Font font) {
            return this.width;
        }
    }
}
