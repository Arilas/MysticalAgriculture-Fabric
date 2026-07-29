package com.blakebr0.mysticalagriculture.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class MysticalAgricultureTags {
    public interface Blocks {
        TagKey<Block> CROPS = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("crops"));

        TagKey<Block> ALWAYS_EFFECTIVE_FARMLAND = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("always_effective_farmland"));

        TagKey<Block> INCORRECT_FOR_INFERIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_inferium_tool"));
        TagKey<Block> INCORRECT_FOR_PRUDENTIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_prudentium_tool"));
        TagKey<Block> INCORRECT_FOR_TERTIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_tertium_tool"));
        TagKey<Block> INCORRECT_FOR_IMPERIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_imperium_tool"));
        TagKey<Block> INCORRECT_FOR_SUPREMIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_supremium_tool"));
        TagKey<Block> INCORRECT_FOR_AWAKENED_SUPREMIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_awakened_supremium_tool"));
        TagKey<Block> INCORRECT_FOR_SOULIUM_TOOL = TagKey.create(Registries.BLOCK, MysticalAgricultureAPI.resource("incorrect_for_soulium_tool"));
    }

    public interface Items {
        TagKey<Item> ESSENCES = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("essences"));
        TagKey<Item> SEEDS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("seeds"));

        TagKey<Item> REPAIRS_INFERIUM_ARMOR = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("repairs_inferium_armor"));
        TagKey<Item> REPAIRS_PRUDENTIUM_ARMOR = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("repairs_prudentium_armor"));
        TagKey<Item> REPAIRS_TERTIUM_ARMOR = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("repairs_tertium_armor"));
        TagKey<Item> REPAIRS_IMPERIUM_ARMOR = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("repairs_imperium_armor"));
        TagKey<Item> REPAIRS_SUPREMIUM_ARMOR = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("repairs_supremium_armor"));
        TagKey<Item> REPAIRS_AWAKENED_SUPREMIUM_ARMOR = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("repairs_awakened_supremium_armor"));

        TagKey<Item> INFERIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("inferium_tool_materials"));
        TagKey<Item> PRUDENTIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("prudentium_tool_materials"));
        TagKey<Item> TERTIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("tertium_tool_materials"));
        TagKey<Item> IMPERIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("imperium_tool_materials"));
        TagKey<Item> SUPREMIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("supremium_tool_materials"));
        TagKey<Item> AWAKENED_SUPREMIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("awakened_supremium_tool_materials"));
        TagKey<Item> SOULIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("soulium_tool_materials"));

        TagKey<Item> MYSTICAL_ENLIGHTENMENT_ENCHANTABLE = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("enchantable/mystical_enlightenment"));
        TagKey<Item> SOUL_SIPHONER_ENCHANTABLE = TagKey.create(Registries.ITEM, MysticalAgricultureAPI.resource("enchantable/soul_siphoner"));
    }
}
