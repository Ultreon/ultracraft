package com.ultreon.craft.collection;

import java.util.Arrays;

class ShortPaletteData implements PaletteData {
    private long[] data;

    public ShortPaletteData(int size) {
        this.data = new long[size / 4];
    }

    public ShortPaletteData(long[] data) {
        this.data = data;
    }

    public long[] getData() {
        return this.data;
    }

    public int get(int idx) {
        if (idx > this.data.length * 4)
            throw new IndexOutOfBoundsException("Index %s out of bounds for length %s".formatted(idx, this.data.length * 4));

        return (short) this.data[idx / 4] >> (idx % 4) & 0xFFFF;
    }

    public void set(int idx, int value) {
        if (idx > this.data.length * 4)
            throw new IndexOutOfBoundsException("Index %s out of bounds for length %s".formatted(idx, this.data.length * 4));

        this.data[idx / 4] |= (long) (value & 0xFFFF) << (idx % 4);
    }

    public int size() {
        return this.data.length * 4;
    }

    @Override
    public void reduce(int value) {
        for (int idx = 0; idx < this.data.length; idx++) {
            // Decrement value if it's higher than the old value
            if (this.get(idx) > value) {
                this.decrement(idx);
            }
        }
    }

    @Override
    public void fill() {
        this.data = new long[this.data.length];
    }

    @Override
    public int rawSize() {
        return this.data.length;
    }

    private void decrement(int idx) {
        this.data[idx / 4] |= ((this.data[idx / 4] >> idx % 4) - 1 & 0xFFFF) << (idx % 4);
    }

    @Override
    public boolean contains(int value) {
        return Arrays.binarySearch(this.data, (short) value) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ShortPaletteData that = (ShortPaletteData) o;
        return Arrays.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public String toString() {
        return "ShortPaletteData{" +
                "size=" + this.data.length +
                '}';
    }
}
