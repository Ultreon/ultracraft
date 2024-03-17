package com.ultreon.craft.client.gui.screens.options;

public enum BooleanEnum {
    TRUE(true),
    FALSE(false);

    private final boolean value;

    BooleanEnum(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    public static BooleanEnum of(boolean value) {
        for (BooleanEnum booleanEnum : BooleanEnum.values()) {
            if (booleanEnum.value == value) {
                return booleanEnum;
            }
        }
        throw new InternalError("Invalid boolean value: " + value);
    }
}
