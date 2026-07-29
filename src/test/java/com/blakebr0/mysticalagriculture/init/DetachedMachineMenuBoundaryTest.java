package com.blakebr0.mysticalagriculture.init;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DetachedMachineMenuBoundaryTest {
    @Test
    void everyPoweredDetachedMenuUsesThePlayerLevelSupplier() throws IOException {
        var source = Files.readString(Path.of(
                "src/main/java/com/blakebr0/mysticalagriculture/init/ModMenuTypes.java"
        ));
        var levelAwareFactory = "createInventoryHandler(null, inventory.player::level)";

        assertEquals(
                6,
                source.split(java.util.regex.Pattern.quote(levelAwareFactory), -1).length - 1,
                "every powered machine menu must construct its detached inventory with the player level"
        );
        assertFalse(
                source.contains("createInventoryHandler(null, () -> null)"),
                "a powered menu factory still supplies no level"
        );
    }
}
