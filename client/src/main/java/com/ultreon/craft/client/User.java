package com.ultreon.craft.client;

public record User(String name) {
    @Override
    public String toString() {
        return name;
    }
}
