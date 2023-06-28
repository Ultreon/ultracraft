package com.ultreon.craft;

import com.ultreon.libs.commons.v0.Identifier;

public class Task implements Runnable {
    private static final Runnable EMPTY = () -> {};
    private final Identifier id;
    private Runnable block = EMPTY;

    public Task(Identifier id) {
        this.id = id;
    }

    public Task(Identifier id, Runnable block) {
        this.id = id;
        this.block = block;
    }

    public Identifier id() {
        return this.id;
    }

    @Override
    public void run() {
        this.block.run();
    }
}
