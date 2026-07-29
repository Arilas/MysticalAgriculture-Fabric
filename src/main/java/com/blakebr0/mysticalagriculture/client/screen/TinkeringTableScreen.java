package com.blakebr0.mysticalagriculture.client.screen;

import com.blakebr0.cucumber.client.screen.BaseContainerScreen;
import com.blakebr0.cucumber.iface.IToggleableSlot;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.container.TinkeringTableContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class TinkeringTableScreen extends BaseContainerScreen<TinkeringTableContainer> {
    private static final Identifier BACKGROUND = MysticalAgriculture.resource("textures/gui/tinkering_table.png");

    public TinkeringTableScreen(TinkeringTableContainer container, Inventory inv, Component title) {
        super(container, inv, title, BACKGROUND, 176, 197);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        gfx.text(this.font, this.title, (this.imageWidth / 2 - this.font.width(this.title) / 2), 6, -12566464, false);
        gfx.text(this.font, this.playerInventoryTitle, 8, (this.imageHeight - 96 + 2), -12566464, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float a) {
        super.extractBackground(gfx, mouseX, mouseY, a);

        int x = this.leftPos;
        int y = this.topPos;

        for (var slot : this.menu.slots) {
            if (slot.isActive() && slot instanceof IToggleableSlot) {
                gfx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + slot.x, y + slot.y, 8, 115, 16, 16, 256, 256);
            }
        }
    }
}
