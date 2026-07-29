package com.blakebr0.mysticalagriculture.handler;

import com.blakebr0.cucumber.inventory.CItemStacksHandler;
import com.blakebr0.cucumber.inventory.SidedInventoryWrapper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A Fabric transfer view that keeps Cucumber's handler transaction and slot
 * policy as the only mutation path.
 */
public final class MachineItemStorage implements SlottedStorage<ItemVariant> {
    private final List<SingleSlotStorage<ItemVariant>> slots;

    private MachineItemStorage(SlotAccess access) {
        var slots = new ArrayList<SingleSlotStorage<ItemVariant>>(access.size());
        for (int slot = 0; slot < access.size(); slot++) {
            slots.add(new Slot(access, slot));
        }
        this.slots = List.copyOf(slots);
    }

    public static MachineItemStorage of(CItemStacksHandler inventory) {
        return new MachineItemStorage(new HandlerAccess(inventory));
    }

    public static @Nullable MachineItemStorage sided(
            SidedInventoryWrapper[] wrappers,
            @Nullable Direction direction
    ) {
        if (direction == null) {
            return null;
        }
        if (wrappers.length < 3) {
            throw new IllegalArgumentException("A machine sided inventory requires UP, DOWN, and NORTH wrappers");
        }

        var wrapper = switch (direction) {
            case UP -> wrappers[0];
            case DOWN -> wrappers[1];
            default -> wrappers[2];
        };
        return new MachineItemStorage(new SidedAccess(wrapper));
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        Objects.requireNonNull(transaction, "transaction");

        long inserted = 0;
        for (var slot : this.slots) {
            inserted += slot.insert(resource, maxAmount - inserted, transaction);
            if (inserted == maxAmount) {
                break;
            }
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        Objects.requireNonNull(transaction, "transaction");

        long extracted = 0;
        for (var slot : this.slots) {
            extracted += slot.extract(resource, maxAmount - extracted, transaction);
            if (extracted == maxAmount) {
                break;
            }
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new ArrayList<StorageView<ItemVariant>>(this.slots).iterator();
    }

    @Override
    public int getSlotCount() {
        return this.slots.size();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return this.slots.get(slot);
    }

    private record Slot(SlotAccess access, int index) implements SingleSlotStorage<ItemVariant> {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.access.insert(this.index, resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.access.extract(this.index, resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return this.access.getStack(this.index).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(this.access.getStack(this.index));
        }

        @Override
        public long getAmount() {
            return this.access.getStack(this.index).getCount();
        }

        @Override
        public long getCapacity() {
            return this.access.getSlotLimit(this.index);
        }
    }

    private interface SlotAccess {
        int size();

        ItemStack getStack(int slot);

        int getSlotLimit(int slot);

        long insert(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction);

        long extract(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction);
    }

    private record HandlerAccess(CItemStacksHandler inventory) implements SlotAccess {
        private HandlerAccess {
            Objects.requireNonNull(inventory, "inventory");
        }

        @Override
        public int size() {
            return this.inventory.getContainerSize();
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.inventory.getItem(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return this.inventory.getSlotLimit(slot);
        }

        @Override
        public long insert(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.inventory.insert(slot, resource, maxAmount, transaction);
        }

        @Override
        public long extract(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.inventory.extract(slot, resource, maxAmount, transaction);
        }
    }

    private record SidedAccess(SidedInventoryWrapper inventory) implements SlotAccess {
        private SidedAccess {
            Objects.requireNonNull(inventory, "inventory");
        }

        @Override
        public int size() {
            return this.inventory.getContainerSize();
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.inventory.getItem(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return this.inventory.getSlotLimit(slot);
        }

        @Override
        public long insert(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.inventory.insert(slot, resource, maxAmount, transaction);
        }

        @Override
        public long extract(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.inventory.extract(slot, resource, maxAmount, transaction);
        }
    }
}
