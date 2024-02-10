package com.ultreon.craft.client.input.keyboard;

public class KeyboardLayout {
    private final char[][] lower;
    private final char[][] upper;

    public KeyboardLayout(char[][] lower, char[][] upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public char getChar(int x, int y, boolean shift) {
        return shift ? upper[x][y] : lower[x][y];
    }

    public char getChar(int x, int y) {
        return getChar(x, y, false);
    }

    public char[][] getLayout(boolean shift) {
        return shift ? upper : lower;
    }
}
