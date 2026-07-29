package com.blakebr0.mysticalagriculture.item;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MachineUpgradeTooltipBoundaryTest {
    @Test
    void shiftDetectionStaysClientOnlyAndRegistersAtClientBoundary() throws IOException {
        var mainRoot = Path.of("src/main/java/com/blakebr0/mysticalagriculture");
        var item = Files.readString(mainRoot.resolve("item/MachineUpgradeItem.java"));
        var client = Files.readString(mainRoot.resolve("client/handler/MachineUpgradeTooltipHandler.java"));
        var initializer = Files.readString(mainRoot.resolve("MysticalAgricultureClient.java"));

        assertFalse(item.contains("net.minecraft.client"),
                "the common machine upgrade item names client-only Minecraft classes");
        assertFalse(item.contains("flag.isAdvanced()"),
                "advanced tooltips still incorrectly stand in for the Shift key");
        assertTrue(item.contains("appendUpgradeDetails"),
                "the common item does not expose loader-neutral detail composition");
        assertTrue(client.contains("Minecraft.getInstance().hasShiftDown()"),
                "the client tooltip callback does not preserve Shift behavior");
        assertTrue(client.contains("ItemTooltipCallback.EVENT.register"),
                "the client tooltip callback is not registered");
        assertTrue(initializer.contains("MachineUpgradeTooltipHandler.register()"),
                "the tooltip handler is not connected to the Fabric client initializer");
    }
}
