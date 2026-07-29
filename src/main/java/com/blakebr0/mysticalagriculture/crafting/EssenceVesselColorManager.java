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

    private final Map<String, Integer> colors = new HashMap<>();

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
    }

    @Override
    public Identifier getFabricId() {
        return MysticalAgricultureAPI.resource("essence_vessel_color_manager");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        this.load(manager);
    }

    public int getColor(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return this.colors.getOrDefault(id.toString(), 0xFFFFFF);
    }

    public void addColor(ItemStack stack, int color) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        this.colors.put(id.toString(), color);
    }

    public void setColors(Map<String, Integer> colors) {
        this.colors.clear();
        this.colors.putAll(colors);
    }

    private void load(ResourceManager manager) {
        var stopwatch = Stopwatch.createStarted();
        var resources = manager.listResources("mysticalagriculture/essence_vessel_colors.json", s -> s.getPath().endsWith(".json"));

        this.colors.clear();

        for (var resource : resources.entrySet()) {
            try (var reader = resource.getValue().openAsReader()) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    var item = entry.getKey();
                    var color = ParsingHelper.parseHex(entry.getValue().getAsString(), item);

                    this.colors.put(item, color);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load {}", resource.getKey(), e);
            }
        }

        LOGGER.info("Loaded {} essence vessel colors in {} ms", this.colors.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
}
