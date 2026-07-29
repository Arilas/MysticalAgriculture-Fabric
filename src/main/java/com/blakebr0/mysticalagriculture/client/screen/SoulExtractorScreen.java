package com.blakebr0.mysticalagriculture.client.screen;

import com.blakebr0.cucumber.client.screen.BaseContainerScreen;
import com.blakebr0.cucumber.client.screen.widget.EnergyBarWidget;
import com.blakebr0.cucumber.util.Formatting;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.container.SoulExtractorContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SoulExtractorScreen extends BaseContainerScreen<SoulExtractorContainer> {
    private static final Identifier BACKGROUND = MysticalAgriculture.resource("textures/gui/soul_extractor.png");

    public SoulExtractorScreen(SoulExtractorContainer container, Inventory inv, Component title) {
        super(container, inv, title, BACKGROUND, 176, 194);
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        this.addRenderableWidget(new EnergyBarWidget(x + 7, y + 17, this.menu::getEnergyStored, this.menu::getEnergyCapacity));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        gfx.text(this.font, this.title, (this.imageWidth / 2 - this.font.width(this.title) / 2), 6, -12566464, false);
        gfx.text(this.font, this.playerInventoryTitle, 8, (this.imageHeight - 96 + 2), -12566464, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        super.extractTooltip(gfx, mouseX, mouseY);

        if (mouseX > x + 30 && mouseX < x + 45 && mouseY > y + 39 && mouseY < y + 53) {
            gfx.setTooltipForNextFrame(this.font, Formatting.energy(this.menu.getFuelLeft()), mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float a) {
        super.extractBackground(gfx, mouseX, mouseY, a);

        int x = this.leftPos;
        int y = this.topPos;

        if (this.menu.getFuelItemValue() > 0) {
            int i = this.getBurnLeftScaled(13);
            gfx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 31, y + 52 - i, 176, 12 - i, 14, i + 1, 256, 256);
        }

        if (this.menu.getProgress() > 0) {
            int i2 = this.getProgressScaled(24);
            gfx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + 98, y + 51, 176, 14, i2 + 1, 16, 256, 256);
        }
    }

    public int getProgressScaled(int pixels) {
        int i = this.menu.getProgress();
        int j = this.menu.getOperationTime();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    public int getBurnLeftScaled(int pixels) {
        int i = this.menu.getFuelLeft();
        int j = this.menu.getFuelItemValue();
        return (int) (j != 0 && i != 0 ? (long) i * pixels / j : 0);
    }
}
