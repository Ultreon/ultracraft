package com.ultreon.craft.collection;

import java.util.Arrays;

class BytePaletteData implements PaletteData {
    private long[] data;

    public BytePaletteData(int size) {
        this.data = new long[size / 8];
    }

    public BytePaletteData(long[] data) {
        this.data = data;
    }

    public long[] getData() {
        return this.data;
    }

    public int get(int idx) {
        if (idx > this.data.length * 8)
            throw new IndexOutOfBoundsException("Index %s out of bounds for length %s".formatted(idx, this.data.length * 8));

        return (byte) this.data[idx / 8] >> (idx % 8) & 0xFF;
    }

    public void set(int idx, int value) {
        if (idx > this.data.length * 8)
            throw new IndexOutOfBoundsException("Index %s out of bounds for length %s".formatted(idx, this.data.length * 8));

        this.data[idx / 8] |= (long) (value & 0xFF) << (idx % 8);
    }

    public int size() {
        return this.data.length * 8;
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
        this.data[idx / 8] |= ((this.data[idx / 8] >> idx % 8) - 1 & 0xFF) << (idx % 8);
    }

    @Override
    public boolean contains(int value) {
        return Arrays.binarySearch(this.data, (byte) value) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        BytePaletteData that = (BytePaletteData) o;
        return Arrays.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public String toString() {
        return "BytePaletteData{" +
                "size=" + this.data.length +
                '}';
    }
}
