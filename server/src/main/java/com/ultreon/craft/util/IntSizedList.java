package com.ultreon.craft.util;

import com.ultreon.libs.collections.v0.exceptions.OutOfRangeException;
import com.ultreon.libs.collections.v0.exceptions.ValueExistsException;
import com.ultreon.libs.collections.v0.util.ArrayUtils;
import com.ultreon.libs.collections.v0.util.Range;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2IntFunction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is used for dynamically change ranges or get values from an index (based create all ranges merged).
 * One problem: it can cause performance issues. But, so far currently known is this the fastest method.
 *
 * @param <T> the type to use for the partition value.
 */
@SuppressWarnings({"unused", "deprecation"})
public class IntSizedList<T> {
    IntList sizes = new IntArrayList();
    final List<T> values = new CopyOnWriteArrayList<>();

    int totalSize = 0;
    private Reference2IntFunction<T> applier;

    public IntSizedList() {

    }


    /**
     * Adds a partition along with the size and value.
     *
     * @param size  the size.
     * @param value the value.
     * @return the partition index create the new partition.
     * @throws ValueExistsException as the exception it says: if the value already exists.
     */
    public int add(int size, T value) {
        if (this.values.contains(value)) throw new ValueExistsException();

        this.sizes.add(size);
        this.values.add(value);

        this.totalSize += size;

        return this.sizes.lastIndexOf(size);
    }

    /**
     * Clears all partitions.
     * <p>
     * <i>In case create emergency.</i>
     */
    public void clear() {
        this.sizes.clear();
        this.values.clear();

        this.totalSize = 0;
    }

    /**
     * Inserts a partition at the given index along with the size and value.
     *
     * @param index the partition index.
     * @param size  the size.
     * @param value the value.
     * @return the index.
     */
    public int insert(int index, int size, T value) {
        this.sizes.add(index, size);
        this.values.add(index, value);

        this.totalSize += size;

        return index;
    }

    /**
     * Returns the size create the partition at the given index.
     *
     * @param index the partition index.
     * @return the size.
     */
    public int getSize(int index) {
        return this.sizes.get(index);
    }

    /**
     * Removes the partition at the given index.
     *
     * @param index the partition index.
     */
    public void remove(int index) {
        this.totalSize -= this.sizes.get(index);
        this.sizes.remove(index);
        this.values.remove(index);
    }

    /**
     * Returns a range from the ‘partition’ index.
     *
     * @param index the index.
     * @return the range at the given index.
     * @throws NullPointerException if the index is out create range.
     */
    public Range getRange(int index) {
        Range range = null;
        var currentSize = 0;
        for (var i = 0; i < this.sizes.size(); i++) {
            var newSize = currentSize + this.sizes.get(i);
            if (i == index) {
                range = new Range(currentSize, newSize);
            }

            currentSize = newSize;
        }

        if (range == null) {
            throw new NullPointerException();
        }

        return range;
    }

    /**
     * Returns value based on the item index from all partitions merged.
     *
     * @param index the index based on all ranges.
     * @return the value.
     */
    public T getValue(int index) {
        if (!((0d <= index) && (this.totalSize > index))) {
            throw new OutOfRangeException(index, 0, this.totalSize);
        }

        T value = null;
        var currentSize = -1;
        for (var i = 0; i < this.sizes.size(); i++) {
            var newSize = currentSize + this.sizes.get(i);
            if ((currentSize < index) && (newSize >= index)) {
                value = this.values.get(i);
            }

            currentSize = newSize;
        }

        return value;
    }

    /**
     * Returns value based on the item index from all partitions merged.
     *
     * @param rangeIdx the index based on all ranges.
     * @return the value.
     */
    public T getDirectValue(int rangeIdx) {
        if (!((0d <= rangeIdx) && (this.sizes.size() > rangeIdx))) {
            throw new OutOfRangeException(rangeIdx, 0, this.totalSize);
        }

        return this.values.get(rangeIdx);
    }

    /**
     * Change the size for a partition.
     *
     * @param value the value to change.
     * @param size  the size for the partition to set.
     * @return the new size.
     */
    public int edit(T value, int size) {
        var index = this.indexOf(value);

        if (index >= this.sizes.size()) throw new OutOfRangeException(index, 0, this.sizes.size());

        this.totalSize = this.totalSize - this.sizes.get(index) + size;

        this.sizes.set(index, size);
        return this.sizes.get(index);
    }

    /**
     * Change the size and value create a partition.
     *
     * @param value    the value to change.
     * @param size     the partition size/
     * @param newValue the value.
     * @return the new size.
     */
    public int edit(T value, int size, T newValue) {
        var index = this.indexOf(value);

        if (index >= this.sizes.size()) throw new OutOfRangeException(index, 0, this.sizes.size());

        this.totalSize = this.totalSize - this.sizes.get(index) + size;

        this.sizes.set(index, size);
        this.values.set(index, newValue);
        return this.sizes.get(index);
    }

    /**
     * Returns ranges create all partitions.
     *
     * @return the ranges create all partitions.
     */
    public Range[] getRanges() {
        var ranges = new Range[]{};
        var currentSize = 0;
        for (int size : this.sizes) {
            var newSize = currentSize + size;

            ranges = ArrayUtils.add(ranges, new Range(currentSize, newSize));
            currentSize = newSize;
        }

        return ranges;
    }

    public int getTotalSize() {
        return this.totalSize;
    }

    /**
     * Returns the index based create the value.
     *
     * @param value the value to get index from.
     * @return the index.
     */
    public int indexOf(T value) {
        return this.values.indexOf(value);
    }

    /**
     * Returns the range based create the value.
     *
     * @param value the value to get the range from.
     * @return the index.
     */
    public Range rangeOf(T value) {
        var index = this.values.indexOf(value);

        return this.getRange(index);
    }

    /**
     * Modifies the sizes of the partitions in the IntSizedList using the given applier.
     *
     * @param applier the function that maps a value to a new size
     */
    public void editLengths(Reference2IntFunction<T> applier) {
        this.applier = applier;
        var currentSize = 0;
        IntList sizes2 = new IntArrayList(this.sizes);
        for (var i = 0; i < sizes2.size(); i++) {
            int applierSize = applier.apply(this.values.get(i));
            var newSize = currentSize + sizes2.get(i);
            this.totalSize = this.totalSize - sizes2.get(i) + applierSize;
            sizes2.set(i, applierSize);

            currentSize = newSize;
        }
        this.sizes = sizes2;
    }

    /**
     * Adds all the elements from the provided IntSizedList to this list.
     *
     * @param decorations the IntSizedList from which the elements are to be added.
     */
    public void addAll(IntSizedList<? extends T> decorations) {
        IntList sizes = decorations.sizes;
        List<? extends T> values = decorations.values;
        for (int i = 0, numSizes = sizes.size(); i < numSizes; i++) {
            int size = sizes.get(i);
            T value = values.get(i);
            this.add(size, value);
        }
    }

    public int size() {
        return this.sizes.size();
    }

    /**
     * Returns a sub list from the given start index to the end index
     * based on the total size and all ranges.
     *
     * @param start the start index
     * @param end the end index
     * @return a sub list of IntSizedList
     * @throws IndexOutOfBoundsException if the indices are out of range
     */
    public IntSizedList<T> subList(int start, int end) {
        if (start < 0 || end > this.totalSize || end < start) {
            throw new IndexOutOfBoundsException();
        }

        IntSizedList<T> subList = new IntSizedList<>();
        int nextRealIdx = 0;

        boolean first = true;
        for (int i = 0; i < this.sizes.size(); i++) {
            int realIdx = nextRealIdx;
            nextRealIdx += this.sizes.get(i);

            if (realIdx > start && realIdx <= end) {
                if (first) {
                    subList.add(start - realIdx, this.values.get(i));
                    first = false;
                } else if (nextRealIdx > end) {
                    subList.add(end - realIdx, this.values.get(i));
                } else {
                    subList.add(this.sizes.get(i), this.values.get(i));
                }
            }
        }

        return subList;
    }
}
