package com.ultreon.craft.entity;

public record Attribute(String key) {
    public static final Attribute SPEED = new Attribute("ultracraft.generic.speed");
    public static final Attribute BLOCK_REACH = new Attribute("ultracraft.generic.block_reach");
    public static final Attribute ENTITY_REACH = new Attribute("ultracraft.generic.entity_reach");
}
