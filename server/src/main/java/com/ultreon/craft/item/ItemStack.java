package com.ultreon.craft.item;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ElementID;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A class that holds items with a certain amount and with data.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @see Item
 */
public class ItemStack {
    private Item item;
    @NotNull
    private MapType data;
    private int count;

    /**
     * @param item the item type to hold.
     */
    public ItemStack(Item item) {
        this(item, 1);
    }

    /**
     *
     */
    public ItemStack() {
        this(Items.AIR, 0);
    }

    /**
     * @param item  the item type to hold.
     * @param count the stack amount.
     */
    public ItemStack(Item item, int count) {
        this(item, count, new MapType());
    }

    /**
     * @param item  the item type to hold.
     * @param count the stack amount.
     * @param data  the data tag.
     */
    public ItemStack(Item item, int count, @NotNull MapType data) {
        this.item = item;
        this.count = count;
        this.data = data;
        this.checkCount(); // Note: used method so mods can use @Redirect to remove stack limits.
    }

    public static ItemStack load(MapType data) {
        @Nullable ElementID id = ElementID.tryParse(data.getString("item"));
        if (id == null) return new ItemStack();

        Item item = Registries.ITEM.getElement(id);
        if (item == null || item == Items.AIR) return new ItemStack();

        int count = data.getInt("count", 0);
        if (count <= 0) return new ItemStack();

        MapType tag = data.getMap("Tag", new MapType());
        return new ItemStack(item, count, tag);
    }

    public MapType save() {
        MapType data = new MapType();
        data.putString("item", this.item.getId().toString());
        data.putInt("count", this.count);
        data.put("Tag", this.data);
        return data;
    }

    private void checkCount() {
        int maxStackSize = this.item.getMaxStackSize();
        if (this.count < 0) this.count = 0;
        if (this.count > maxStackSize) this.count = maxStackSize;
    }

    public static ItemStack empty() {
        return new ItemStack() {
            @Override
            public void setCount(int count) {

            }
        };
    }

    /**
     * @return get the item the stack is holding.
     */
    public Item getItem() {
        return this.item;
    }

    /**
     * @return the item stack's data tag.
     */
    public @NotNull MapType getData() {
        return this.data;
    }

    public void setData(@NotNull MapType data) {
        this.data = data;
    }

    /**
     * @return the item stack count.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @param count the item stack count to set.
     */
    public void setCount(int count) {
        this.count = count;
        if (this.count == 0) {
            this.item = Items.AIR;
        }
    }

    /**
     * Shrinks the item stack by an amount.
     *
     * @param amount the amount to shrink by.
     * @return the amount that was remains.
     */
    @CanIgnoreReturnValue
    public int shrink(int amount) {
        Preconditions.checkArgument(amount >= 0, "Grow amount should not be negative!");
        if (this.count == 0) {
            return amount;
        }

        if (this.count - amount <= 0) {
            var remainder = amount - this.count;
            this.count = 0;
            this.item = Items.AIR;
            return remainder;
        } else {
            this.count -= amount;
            return 0;
        }
    }

    /**
     * Grows the item stack by an amount.
     *
     * @param amount the amount to grow by.
     * @return the amount of item that has overflown.
     */
    @CanIgnoreReturnValue
    public int grow(int amount) {
        Preconditions.checkArgument(amount >= 0, "Grow amount should not be negative!");
        if (this.count == this.getItem().getMaxStackSize())
            return amount;

        if (this.count + amount >= this.getItem().getMaxStackSize()) {
            var overflown = this.count + amount - this.getItem().getMaxStackSize();
            this.count = this.getItem().getMaxStackSize();
            return overflown;
        } else {
            this.count += amount;
            return 0;
        }
    }

    /**
     * @return true if this item stack is empty.
     */
    public boolean isEmpty() {
        return this.item == Items.AIR || this.count < 1;
    }

    /**
     * @return a copy of this item stack.
     */
    public ItemStack copy() {
        return new ItemStack(this.item, this.count, this.data.copy());
    }

    /**
     * Gets the description of the item.
     *
     * @return the description
     */
    public List<TextObject> getDescription() {
        return this.item.getDescription(this);
    }

    /**
     * Determines if this ItemStack is similar to another ItemStack.
     * Checks the item and the data tag.
     *
     * @param other the ItemStack to compare with
     * @return true if the ItemStacks are similar, false otherwise
     */
    public boolean sameItemSameData(ItemStack other) {
        return this.item == other.item && this.data.equals(other.data);
    }

    /**
     * Checks if the current item is the same as the given item.
     *
     * @param other the item to compare with
     * @return true if the items are the same, false otherwise
     */
    public boolean isSameItem(ItemStack other) {
        return this.item == other.item;
    }

    /**
     * Split the item with a specified amount.
     *
     * @param amount the amount to split.
     * @return the part that got split.
     */
    public ItemStack split(int amount) {
        if (amount <= 0) return ItemStack.empty(); // Return an empty stack if the amount is invalid

        if (amount >= this.count) {
            ItemStack copy = this.copy();
            this.count = 0;
            return copy;
        } else {
            this.count -= amount;
            return new ItemStack(this.item, amount, this.data.copy());
        }
    }

    /**
     * Split the item stack in half.
     *
     * @return the other half of the item stack.
     */
    public ItemStack split() {
        if (this.count <= 1) return ItemStack.empty();
        return this.split(this.count / 2);
    }

    /**
     * Transfers a specified amount of the stack to another item stack.
     *
     * @param target the item stack to receive the items.
     * @param amount the amount to transfer.
     * @return the amount remaining.
     */
    @CanIgnoreReturnValue
    public int transferTo(ItemStack target, int amount) {
        Preconditions.checkArgument(amount >= 0, "The transfer amount should be non-negative.");
        Preconditions.checkArgument(amount <= this.count, "Can't transfer more than the current stack count.");

        if (target.isEmpty()) {
            target.item = this.item;
            target.data = this.data.copy();
            target.count = amount;
            this.shrink(amount);
            return 0;
        }

        Preconditions.checkArgument(this.item == target.item, "The item of source stack should match the i destination stack.");
        Preconditions.checkArgument(this.data.equals(target.data), "The tag of source stack should match the tag of the destination stack.");

        int remainder = target.grow(amount);
        if (remainder == 0) {
            this.shrink(amount);
            return 0;
        }

        int transferred = amount - remainder;
        this.shrink(transferred);
        return remainder;
    }

    /**
     * Transfers one item to another item stack.
     *
     * @param target the item stack to receive the item.
     * @return the amount remaining.
     */
    @CanIgnoreReturnValue
    public boolean transferTo(ItemStack target) {
        if (target.getItem() != this.item) return false;
        if (target.grow(1) == 1) return false;

        this.shrink(1);
        return true;
    }

    public String toString() {
        return this.item.getId() + " x" + this.count;
    }

    public ItemStack merge(ItemStack with) {
        if (!this.sameItemSameData(with)) return with;

        if (this.count + with.count > this.getItem().getMaxStackSize()) {
            with.count = this.getItem().getMaxStackSize() - this.count;
            this.count = this.getItem().getMaxStackSize();
            return with;
        }

        this.count += with.count;
        return with;
    }
}
