package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.cucumber.helper.ParsingHelper;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.google.common.base.Stopwatch;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

public class EssenceVesselColorManager implements SimpleSynchronousResourceReloadListener {
    public static final EssenceVesselColorManager INSTANCE = new EssenceVesselColorManager();
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger("Mystical Agriculture");

    private volatile Map<String, Integer> colors = Map.of();
    private Map<String, Integer> pendingColors;

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
    }

    @Override
    public Identifier getFabricId() {
        return MysticalAgricultureAPI.resource("essence_vessel_color_manager");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        this.stage(manager);
    }

    public int getColor(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return this.colors.getOrDefault(id.toString(), 0xFFFFFF);
    }

    public void addColor(ItemStack stack, int color) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var updated = new HashMap<>(this.colors);
        updated.put(id.toString(), color);
        this.colors = Map.copyOf(updated);
    }

    public void setColors(Map<String, Integer> colors) {
        this.colors = Map.copyOf(colors);
    }

    public Map<String, Integer> copyColors() {
        return this.colors;
    }

    public void finishReload() {
        if (this.pendingColors != null) {
            this.setColors(this.pendingColors);
            this.pendingColors = null;
        }
    }

    private void stage(ResourceManager manager) {
        var stopwatch = Stopwatch.createStarted();
        var resources = manager.listResources("mysticalagriculture/essence_vessel_colors.json", s -> s.getPath().endsWith(".json"));
        var nextColors = new HashMap<String, Integer>();
        var failed = false;

        for (var resource : resources.entrySet()) {
            try (var reader = resource.getValue().openAsReader()) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var item = entry.getKey();
                    var color = ParsingHelper.parseHex(entry.getValue().getAsString(), item);

                    nextColors.put(item, color);
                }
            } catch (IOException | RuntimeException e) {
                failed = true;
                LOGGER.error("Failed to load {}", resource.getKey(), e);
            }
        }

        if (failed) {
            this.pendingColors = null;
            LOGGER.warn("Keeping {} previously valid essence vessel colors", this.colors.size());
        } else {
            this.pendingColors = Map.copyOf(nextColors);
            LOGGER.info("Staged {} essence vessel colors in {} ms", nextColors.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }
}
