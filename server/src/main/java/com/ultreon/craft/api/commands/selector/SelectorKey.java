package com.ultreon.craft.api.commands.selector;

public enum SelectorKey {

    TAG('#'), TYPE('$'), NAME('@'), DATA('?'), UUID(':'), ID('%'), DISPLAY_NAME('='), CUSTOM_NAME('+');

    private final char key;

    private SelectorKey(char key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return Character.toString(this.key);
    }

    public char symbol() {
        return this.key;
    }

    public static SelectorKey fromKey(char key) {
        for (SelectorKey k : values()) {
            if (k.symbol() == key) {
                return k;
            }
        }
        return null;
    }
}