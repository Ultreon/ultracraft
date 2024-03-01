package com.ultreon.craft.util;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Task <T> implements Runnable {
    private final Identifier id;
    private Supplier<@Nullable T> block = () -> null;
    @Nullable
    CompletableFuture<T> future;

    public Task(Identifier id) {
        this.id = id;
    }

    public Task(Identifier id, Supplier<T> block) {
        this.id = id;
        this.block = block;
    }

    public Task(Identifier id, Runnable block) {
        this.id = id;
        this.block = () -> {
            block.run();
            return null;
        };
    }

    public Identifier id() {
        return this.id;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void run() {
        @Nullable T obj = this.block.get();
        if (this.future != null) {
            this.future.complete(obj);
        }
    }

    public @Nullable T get() {
        return this.future.getNow(null);
    }
}
