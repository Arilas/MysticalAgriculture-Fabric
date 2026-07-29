package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.client.properties.ExperienceCapsuleProperty;
import com.blakebr0.mysticalagriculture.client.properties.SoulJarProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;

public final class ItemModelPropertyHandler {
    private static boolean registered;

    private ItemModelPropertyHandler() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        RangeSelectItemModelProperties.ID_MAPPER.put(MysticalAgriculture.resource("experience_capsule"), ExperienceCapsuleProperty.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(MysticalAgriculture.resource("soul_jar"), SoulJarProperty.MAP_CODEC);
        registered = true;
    }
}
