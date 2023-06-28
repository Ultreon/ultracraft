package com.ultreon.craft.input.util;

public class Trigger {
    public float value;

    public Trigger cpy() {
        Trigger trigger = new Trigger();
        trigger.value = this.value;
        return trigger;
    }
}
