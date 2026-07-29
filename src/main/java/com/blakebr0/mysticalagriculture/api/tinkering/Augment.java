package com.blakebr0.mysticalagriculture.api.tinkering;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.lib.AbilityCache;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * The default implementation of an Augment
 * <p>
 * Extend this class for your augments
 */
public class Augment {
    private final Identifier id;
    private final ResourceKey<Item> item;
    private final int tier;
    private final EnumSet<AugmentType> types;
    private int primaryColor;
    private int secondaryColor;
    private boolean enabled;

    public Augment(Identifier id, int tier, EnumSet<AugmentType> types, int primaryColor, int secondaryColor) {
        this.id = id;
        this.item = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MysticalAgricultureAPI.MOD_ID, id.getPath() + "_augment")
        );
        this.tier = tier;
        this.types = types;
        this.primaryColor = ARGB.color(255, primaryColor);
        this.secondaryColor = ARGB.color(255, secondaryColor);
        this.enabled = true;
    }

    /**
     * The id of this augment, the modid is taken from the namespace for {@link Augment#getModId()},
     * and the path is used for {@link Augment#getName()}
     * @return the id of this augment
     */
    public Identifier getId() {
        return this.id;
    }

    /**
     * The internal name of this augment.
     * This is used for registration, so it MUST be all lowercase with underscores for spaces
     * @return the internal name of this augment
     */
    public String getName() {
        return this.getId().getPath();
    }

    /**
     * The modid of the mod that registered this augment
     * @return the modid of this augment
     */
    public String getModId() {
        return this.getId().getNamespace();
    }

    /**
     * Used to get the internal name of this augment with an _suffix
     * @param suffix the suffix to append (without the initial underscore)
     * @return the name with _suffix
     */
    public String getNameWithSuffix(String suffix) {
        return String.format("%s_%s", this.getName(), suffix);
    }

    /**
     * Get the localized name of this augment using the key augment.{@link Augment#getModId()}.{@link Augment#getName()}
     * @return the localized name of this augment
     */
    public MutableComponent getDisplayName() {
        return Component.translatable(String.format("augment.%s.%s", this.getModId(), this.getName()));
    }

    /**
     * Get the localized description of this augment using the key augment.{@link Augment#getModId()}.{@link Augment#getName()}.description
     * @return the localized description of this augment
     */
    public MutableComponent getDescriptionDisplayText() {
        return Component.translatable(String.format("augment.%s.%s.description", this.getModId(), this.getName())).withStyle(ChatFormatting.GRAY);
    }

    /**
     * The augment types that this augment represents
     * @return applicable augment types
     */
    public EnumSet<AugmentType> getAugmentTypes() {
        return this.types;
    }

    /**
     * The tier of this augment, used to define the minimum tier tinkerable required
     * @return the numerical tier
     */
    public int getTier() {
        return this.tier;
    }

    /**
     * Get the augment item for this augment
     * @return the augment item
     */
    public Item getItem() {
        return BuiltInRegistries.ITEM.getValueOrThrow(this.item);
    }

    /**
     * The primary color of this augment (for the lighter middle areas of the augment)
     * @return the primary color
     */
    public int getPrimaryColor() {
        return this.primaryColor;
    }

    /**
     * Set the primary color of this augment
     * @param color the primary color
     * @return this augment
     */
    public Augment setPrimaryColor(int color) {
        this.primaryColor = color;
        return this;
    }

    /**
     * The secondary color of this augment (for the darker areas of the augment)
     * @return the secondary color
     */
    public int getSecondaryColor() {
        return this.secondaryColor;
    }

    /**
     * Set the secondary color of this augment
     * @param color the secondary color
     * @return this augment
     */
    public Augment setSecondaryColor(int color) {
        this.secondaryColor = color;
        return this;
    }

    /**
     * Should this augment have the enchantment glint?
     * @return has glint effect
     */
    public boolean hasEffect() {
        return this.getTier() >= 5;
    }

    /**
     * Whether this augment has recipes and shows up in the creative menu
     * @return is this augment enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set whether this augment should be hidden from the game
     * @param enabled the enabled state
     */
    public Augment setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Called when the item is used while targeting a block, {@link Item#useOn(UseOnContext)}
     * @param context the item use context
     * @return was the action successful?
     */
    public boolean onItemUse(UseOnContext context) {
        return false;
    }

    /**
     * Called when the item is right-clicked while not targeting a block, {@link Item#use(Level, Player, InteractionHand)}
     * @param stack the item
     * @param level the level
     * @param player the player
     * @param hand the hand
     * @return was the action successful?
     */
    public boolean onRightClick(ItemStack stack, Level level, Player player, InteractionHand hand) {
        return false;
    }

    /**
     * Called when this item is right-clicked on an entity, {@link Item#interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)}
     * @param stack the item
     * @param player the player
     * @param target the clicked entity
     * @param hand the hand
     * @return was the action successful?
     */
    public boolean onRightClickEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return false;
    }

    /**
     * Called when this item is used to attack an entity, {@link Item#hurtEnemy(ItemStack, LivingEntity, LivingEntity)}
     * @param stack the item
     * @param target the attacked entity
     * @param attacker the attacking entity
     */
    public void onHitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) { }

    /**
     * Called when a block is destroyed using this item, {@link Item#mineBlock(ItemStack, Level, BlockState, BlockPos, LivingEntity)}
     * @param stack the item
     * @param level the level
     * @param state the block destroyed
     * @param pos the pos of the block destroyed
     * @param entity the entity that destroyed the block
     * @return was the action successful?
     */
    public boolean onBlockDestroyed(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        return false;
    }

    /**
     * Called when the item is ticked in the player's inventory, {@link Item#inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot)}
     * @param stack the item
     * @param level the level
     * @param entity the player
     * @param slot the slot
     */
    public void onInventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) { }

    /**
     * Called every tick for equipped armor, {@link Item#inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot)}
     * @param stack the item
     * @param level the level
     * @param player the player
     */
    public void onArmorTick(ItemStack stack, ServerLevel level, Player player) { }

    /**
     * Called every tick for equipped armor, meant for player ability changes.
     * @param level the level
     * @param player the player
     * @param cache the ability to cache
     */
    public void onPlayerTick(Level level, Player player, AbilityCache cache) { }

    /**
     * Called when the player hits the ground.
     * @param level the level
     * @param player the player
     * @param fallDistance the distance the player fell
     * @return {@code true} when the registered server damage callback should cancel fall damage
     */
    public boolean onPlayerFall(Level level, Player player, float fallDistance) {
        return false;
    }

    /**
     * The list of {@link AttributeModifier}s that will be automatically applied when equipping this augment
     * @return the attribute modifiers that this augment will apply to the item
     */
    public List<AugmentAttributeModifier> getAttributeModifiers() {
        return List.of();
    }

    /**
     * Does this augment have a set bonus?
     * @return has set bonus
     */
    public boolean hasSetBonus() {
        return false;
    }

    /**
     * Get the localized text of the set bonus using the key augment.{@link Augment#getModId()}.{@link Augment#getName()}.set_bonus
     * @return the localized text of the set bonus
     */
    public MutableComponent getSetBonusDisplayText() {
        return Component.translatable(String.format("augment.%s.%s.set_bonus", this.getModId(), this.getName())).withStyle(ChatFormatting.GRAY);
    }
}
