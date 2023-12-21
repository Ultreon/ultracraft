package com.ultreon.craft.collection;

interface PaletteData {
    int get(int idx);
    void set(int idx, int value);
    long[] getData();

    boolean contains(int value);

    int size();

    void reduce(int value);

    void fill();

    int rawSize();
}
