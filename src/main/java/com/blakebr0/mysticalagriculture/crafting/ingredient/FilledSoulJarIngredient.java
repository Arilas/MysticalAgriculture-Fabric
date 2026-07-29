package com.blakebr0.mysticalagriculture.crafting.ingredient;

import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.init.ModIngredientTypes;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.item.SoulJarItem;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.Stream;

public class FilledSoulJarIngredient implements ICustomIngredient {
    public static final MapCodec<FilledSoulJarIngredient> CODEC = MapCodec.unit(FilledSoulJarIngredient::new);

    private HolderSet<Item> values;

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack != null) {
            return stack.getItem() instanceof SoulJarItem && MobSoulUtils.getSouls(stack) > 0;
        }

        return false;
    }

    @Override
    public Stream<Holder<Item>> items() {
        if (this.values == null) {
            var values = new ArrayList<Holder<Item>>();

            for (var type : MobSoulTypeRegistry.getInstance().getMobSoulTypes()) {
                values.add(MobSoulUtils.getFilledSoulJar(type, ModItems.SOUL_JAR).typeHolder());
            }

            this.values = HolderSet.direct(values);
        }

        return this.values.stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.FILLED_SOUL_JAR.get();
    }

    public static Ingredient of() {
        return new FilledSoulJarIngredient().toVanilla();
    }
}
