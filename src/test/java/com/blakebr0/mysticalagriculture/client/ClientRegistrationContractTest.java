package com.blakebr0.mysticalagriculture.client;

import com.blakebr0.mysticalagriculture.client.handler.AOEAugmentClientHandler;
import com.blakebr0.mysticalagriculture.client.handler.AugmentTooltipHandler;
import com.blakebr0.mysticalagriculture.client.handler.ItemModelPropertyHandler;
import com.blakebr0.mysticalagriculture.client.handler.ModelHandler;
import com.blakebr0.mysticalagriculture.client.handler.TintSourceHandler;
import com.blakebr0.mysticalagriculture.client.properties.ExperienceCapsuleProperty;
import com.blakebr0.mysticalagriculture.client.properties.SoulJarProperty;
import com.blakebr0.mysticalagriculture.client.tints.AugmentTintSource;
import com.blakebr0.mysticalagriculture.client.tints.CropTintSource;
import com.blakebr0.mysticalagriculture.client.tints.InfusionCrystalTintSource;
import com.blakebr0.mysticalagriculture.client.tints.SoulJarTintSource;
import com.blakebr0.mysticalagriculture.item.AugmentItem;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ClientRegistrationContractTest {
    @BeforeAll
    public static void bootstrapVanillaRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void propertyAndTintCodecsAreInstalledInTheVanillaMappers() {
        ItemModelPropertyHandler.register();
        TintSourceHandler.register();

        assertInstanceOf(ExperienceCapsuleProperty.class, decodeProperty("""
                {"property":"mysticalagriculture:experience_capsule"}
                """));
        assertInstanceOf(SoulJarProperty.class, decodeProperty("""
                {"property":"mysticalagriculture:soul_jar"}
                """));
        assertInstanceOf(AugmentTintSource.class, decodeTint("""
                {"type":"mysticalagriculture:augment","id":"mysticalagriculture:test","index":0}
                """));
        assertInstanceOf(CropTintSource.class, decodeTint("""
                {"type":"mysticalagriculture:crop","id":"mysticalagriculture:test","tint_type":"seed"}
                """));
        assertInstanceOf(InfusionCrystalTintSource.class, decodeTint("""
                {"type":"mysticalagriculture:infusion_crystal"}
                """));
        assertInstanceOf(SoulJarTintSource.class, decodeTint("""
                {"type":"mysticalagriculture:soul_jar"}
                """));
    }

    @Test
    public void augmentTooltipDataResolvesThroughTheFabricFactory() {
        ModClientTooltipComponentFactories.register();

        var data = new AugmentItem.AugmentTooltipData(List.of());
        var component = ClientTooltipComponentCallback.EVENT.invoker().getClientComponent(data);

        assertInstanceOf(AugmentTooltipHandler.AugmentToolTypesComponent.class, component);
        assertEquals(0,
                ((AugmentTooltipHandler.AugmentToolTypesComponent) component).stacks().size());
    }

    @Test
    public void cropModelPluginIsRegisteredExactlyOnce() {
        var before = ModelLoadingPlugin.getAll().size();

        ModelHandler.register();
        ModelHandler.register();

        assertEquals(before + 1, ModelLoadingPlugin.getAll().size());
    }

    @Test
    public void aoeArrowInputOnlyEmitsOnNewPressEdges() {
        var state = new AOEAugmentClientHandler.KeyEdgeState();

        assertEquals(AOEAugmentClientHandler.OffsetDelta.HORIZONTAL_NEGATIVE,
                state.poll(true, true, false, false, false));
        assertEquals(AOEAugmentClientHandler.OffsetDelta.NONE,
                state.poll(true, true, false, false, false));
        assertEquals(AOEAugmentClientHandler.OffsetDelta.NONE,
                state.poll(true, false, false, false, false));
        assertEquals(AOEAugmentClientHandler.OffsetDelta.HORIZONTAL_NEGATIVE,
                state.poll(true, true, false, false, false));
        assertEquals(AOEAugmentClientHandler.OffsetDelta.NONE,
                state.poll(false, true, false, false, false));
    }

    private static Object decodeProperty(String json) {
        return RangeSelectItemModelProperties.MAP_CODEC.codec()
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();
    }

    private static Object decodeTint(String json) {
        return ItemTintSources.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();
    }
}
