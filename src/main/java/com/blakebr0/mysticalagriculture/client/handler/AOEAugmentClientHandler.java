package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.mysticalagriculture.api.tinkering.AOEAugment;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.network.payloads.UpdateAOEAugmentOffsetPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

public final class AOEAugmentClientHandler {
    private static final KeyEdgeState KEY_EDGES = new KeyEdgeState();
    private static boolean registered;

    private AOEAugmentClientHandler() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        ClientTickEvents.END_CLIENT_TICK.register(AOEAugmentClientHandler::onClientTick);
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(AOEAugmentClientHandler::beforeBlockOutline);
        registered = true;
    }

    private static void onClientTick(Minecraft minecraft) {
        var window = minecraft.getWindow();
        var control = isControlDown(minecraft);
        var delta = KEY_EDGES.poll(
                control,
                InputConstants.isKeyDown(window, InputConstants.KEY_LEFT),
                InputConstants.isKeyDown(window, InputConstants.KEY_RIGHT),
                InputConstants.isKeyDown(window, InputConstants.KEY_DOWN),
                InputConstants.isKeyDown(window, InputConstants.KEY_UP)
        );

        if (delta == OffsetDelta.NONE || minecraft.gui.screen() != null || minecraft.level == null || minecraft.player == null)
            return;

        var stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof ITinkerable) || AugmentUtils.getMaxAOEAugmentRange(stack) == 0)
            return;

        if (ClientPlayNetworking.canSend(UpdateAOEAugmentOffsetPayload.TYPE)) {
            ClientPlayNetworking.send(new UpdateAOEAugmentOffsetPayload(delta.horizontal(), delta.vertical()));
        }
    }

    private static boolean beforeBlockOutline(LevelRenderContext context, BlockOutlineRenderState outline) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || !(minecraft.hitResult instanceof BlockHitResult hitResult) || !isControlDown(minecraft))
            return true;

        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ITinkerable))
            return true;

        int range = AugmentUtils.getMaxAOEAugmentRange(stack);
        if (range == 0)
            return true;

        var direction = player.isCrouching() ? Direction.UP : hitResult.getDirection();
        var color = player.isCrouching()
                ? ARGB.colorFromFloat(0.7F, 1.0F, 0.5F, 0.0F)
                : ARGB.colorFromFloat(0.7F, 0.0F, 1.0F, 0.0F);
        var camera = context.levelState().cameraRenderState.pos;
        var lineWidth = context.gameRenderer().gameRenderState().windowRenderState.appropriateLineWidth;
        var poseStack = context.poseStack();

        AOEAugment.getAOEBlocks(stack, range, hitResult.getBlockPos(), direction, player).forEach(pos -> {
            if (player.level().getBlockState(pos).isAir())
                return;

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            context.submitNodeCollector().submitShapeOutline(
                    poseStack,
                    Shapes.block(),
                    RenderTypes.lines(),
                    color,
                    lineWidth,
                    false
            );
            poseStack.popPose();
        });

        return true;
    }

    private static boolean isControlDown(Minecraft minecraft) {
        var window = minecraft.getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL)
                || InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL);
    }

    public enum OffsetDelta {
        NONE(0, 0),
        HORIZONTAL_NEGATIVE(-1, 0),
        HORIZONTAL_POSITIVE(1, 0),
        VERTICAL_NEGATIVE(0, -1),
        VERTICAL_POSITIVE(0, 1);

        private final int horizontal;
        private final int vertical;

        OffsetDelta(int horizontal, int vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public int horizontal() {
            return this.horizontal;
        }

        public int vertical() {
            return this.vertical;
        }
    }

    public static final class KeyEdgeState {
        private boolean left;
        private boolean right;
        private boolean down;
        private boolean up;

        public OffsetDelta poll(boolean control, boolean left, boolean right, boolean down, boolean up) {
            OffsetDelta delta = OffsetDelta.NONE;
            if (control) {
                if (left && !this.left) {
                    delta = OffsetDelta.HORIZONTAL_NEGATIVE;
                } else if (right && !this.right) {
                    delta = OffsetDelta.HORIZONTAL_POSITIVE;
                } else if (down && !this.down) {
                    delta = OffsetDelta.VERTICAL_NEGATIVE;
                } else if (up && !this.up) {
                    delta = OffsetDelta.VERTICAL_POSITIVE;
                }
            }

            this.left = left;
            this.right = right;
            this.down = down;
            this.up = up;
            return delta;
        }
    }
}
