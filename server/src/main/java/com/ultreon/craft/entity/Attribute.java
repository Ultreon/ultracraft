package com.ultreon.craft.entity;

public record Attribute(String key) {
    public static final Attribute SPEED = new Attribute("ultracraft.generic.speed");
}
