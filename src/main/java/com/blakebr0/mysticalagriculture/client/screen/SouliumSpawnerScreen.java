package com.blakebr0.mysticalagriculture.client.screen;

import com.blakebr0.cucumber.client.screen.BaseContainerScreen;
import com.blakebr0.cucumber.client.screen.widget.EnergyBarWidget;
import com.blakebr0.cucumber.util.Formatting;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.container.SouliumSpawnerContainer;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SouliumSpawnerScreen extends BaseContainerScreen<SouliumSpawnerContainer> {
    private static final Identifier BACKGROUND = MysticalAgriculture.resource("textures/gui/soulium_spawner.png");
    private SouliumSpawnerTileEntity tile;

    public SouliumSpawnerScreen(SouliumSpawnerContainer container, Inventory inv, Component title) {
        super(container, inv, title, BACKGROUND, 176, 194);
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        this.tile = this.getTileEntity();

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

        if (this.menu.getFuelLeft() > 0 && mouseX > x + 30 && mouseX < x + 45 && mouseY > y + 39 && mouseY < y + 53) {
            gfx.setTooltipForNextFrame(this.font, Formatting.energy(this.menu.getFuelLeft()), mouseX, mouseY);
        }

        if (this.tile != null) {
            var displayEntity = this.tile.getDisplayEntity();

            if (displayEntity != null && isHoveringSlot(x + 134, y + 52, mouseX, mouseY)) {
                var entity = displayEntity.entity();
                var chance = displayEntity.chance();

                var text = Component.empty().append(entity.getDisplayName()).append(" (%.2f%%)".formatted(chance));

                gfx.setTooltipForNextFrame(this.font, text, mouseX, mouseY);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(gfx, mouseX, mouseY, partialTicks);

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

        this.extractEntityPreview(gfx, mouseX, mouseY, partialTicks);
    }

    private SouliumSpawnerTileEntity getTileEntity() {
        var level = this.minecraft.level;

        if (level != null) {
            var tile = level.getBlockEntity(this.getMenu().getBlockPos());

            if (tile instanceof SouliumSpawnerTileEntity spawner) {
                return spawner;
            }
        }

        return null;
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

    private void extractEntityPreview(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {
        if (this.tile == null)
            return;

        var displayEntity = this.tile.getDisplayEntity();
        if (displayEntity == null)
            return;

        var entity = displayEntity.entity();

        float scale = 16.0F;
        float bbMax = Math.max(entity.getBbWidth(), entity.getBbHeight());

        if ((double) bbMax > 1.0D) {
            scale /= bbMax;
        }

        var entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        var entityRenderState = entityRenderer.extractEntity(entity, partialTicks);

        if (entityRenderState instanceof LivingEntityRenderState livingRenderState) {
            livingRenderState.bodyRot = 160.0F;
            livingRenderState.yRot = 0F;
            livingRenderState.xRot = 0F;

            livingRenderState.boundingBoxWidth = livingRenderState.boundingBoxWidth / livingRenderState.scale;
            livingRenderState.boundingBoxHeight = livingRenderState.boundingBoxHeight / livingRenderState.scale;
            livingRenderState.scale = 1.0F;
        }

        var translation = new Vector3f(0.0F, entityRenderState.boundingBoxHeight / 2.0F, 0.0F);
        var rotation = new Quaternionf().rotateY(20F).rotateZ((float) Math.PI);

        int x = this.leftPos;
        int y = this.topPos;

        if (isHoveringSlot(x + 134, y + 52, mouseX, mouseY)) {
            extractSlotHighlightBack(gfx, x + 130, y + 48);
        }

        gfx.entity(entityRenderState, scale, translation, rotation, null, this.leftPos + 122, this.topPos + 33, this.leftPos + 162, this.topPos + 88);

        if (isHoveringSlot(x + 134, y + 52, mouseX, mouseY)) {
            extractSlotHighlightFront(gfx, x + 130, y + 48);
        }
    }
}
