package com.blakebr0.mysticalagriculture.block;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CommonMachineBlockBoundaryTest {
    private static final List<String> MACHINE_BLOCKS = List.of(
            "EssenceFurnaceBlock.java",
            "ReprocessorBlock.java",
            "SoulExtractorBlock.java",
            "HarvesterBlock.java",
            "OreInfuserBlock.java",
            "SouliumSpawnerBlock.java"
    );

    @Test
    void commonMachineBlocksDoNotNameClientOnlyClasses() throws IOException {
        var sourceRoot = Path.of("src/main/java/com/blakebr0/mysticalagriculture/block");

        for (var file : MACHINE_BLOCKS) {
            var source = Files.readString(sourceRoot.resolve(file));

            assertFalse(source.contains("com.blakebr0.mysticalagriculture.client"),
                    () -> file + " names a client-only Mystical Agriculture class");
            assertFalse(source.contains("net.minecraft.client"),
                    () -> file + " names a Minecraft client-only class");
        }
    }
}
