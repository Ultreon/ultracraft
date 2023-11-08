package com.ultreon.craft.item;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A class that hold items with a certain amount and with data.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @see Item
 */
public class ItemStack {
    private Item item;
    @NotNull
    private MapType tag;
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
     * @param item the item type to hold.
     * @param count the stack amount.
     */
    public ItemStack(Item item, int count) {
        this(item, count, new MapType());
    }

    /**
     * @param item the item type to hold.
     * @param count the stack amount.
     * @param tag the data tag.
     */
    public ItemStack(Item item, int count, @NotNull MapType tag) {
        this.item = item;
        this.count = count;
        this.tag = tag;
        this.checkCount(); // Note: used method so mods can use @Redirect to remove stack limits.
    }

    public static ItemStack load(MapType data) {
        @Nullable Identifier id = Identifier.tryParse(data.getString("item"));
        if (id == null) return new ItemStack();

        Item item = Registries.ITEMS.getValue(id);
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
        data.put("Tag", this.tag);
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
    public @NotNull MapType getTag() {
        return this.tag;
    }

    public void setTag(@NotNull MapType tag) {
        this.tag = tag;
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
     * @return the amount of item that have overflown.
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
        return new ItemStack(this.item, this.count, this.tag.copy());
    }

    public List<TextObject> getDescription() {
        return this.item.getDescription(this);
    }

    public boolean isSameItemSameTag(ItemStack other) {
        return this.item == other.item && this.tag.equals(other.tag);
    }

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
            return new ItemStack(this.item, amount, this.tag.copy());
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
            target.tag = this.tag.copy();
        }

        Preconditions.checkArgument(this.item == target.item, "The item of source stack should match the i destination stack.");
        Preconditions.checkArgument(this.tag.equals(target.tag), "The tag of source stack should match the tag of the destination stack.");

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
        if (target.grow(1) == 1) return false;

        this.shrink(1);
        return true;
    }

    public String toString() {
        return this.item.getId() + " x" + this.count;
    }
}
