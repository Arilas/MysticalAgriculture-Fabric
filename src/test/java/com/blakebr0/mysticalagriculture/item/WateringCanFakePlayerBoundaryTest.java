package com.blakebr0.mysticalagriculture.item;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WateringCanFakePlayerBoundaryTest {
    @Test
    void wateringUsesCanonicalFabricFakePlayerBoundary() throws IOException {
        var source = Files.readString(Path.of(
                "src/main/java/com/blakebr0/mysticalagriculture/item/WateringCanItem.java"
        ));

        assertTrue(source.contains("import net.fabricmc.fabric.api.entity.FakePlayer;"),
                "watering does not import Fabric API's canonical fake player");
        assertTrue(source.contains("player instanceof FakePlayer"),
                "watering does not use Fabric API's canonical instanceof boundary");
        assertFalse(source.contains("player.getClass() != ServerPlayer.class"),
                "watering still treats every ServerPlayer subclass as a fake player");
    }
}
