package com.ultreon.craft.util;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Task <T> implements Runnable {
    private final ElementID id;
    private Supplier<@Nullable T> block = () -> null;
    @Nullable
    CompletableFuture<T> future;

    public Task(ElementID id) {
        this.id = id;
    }

    public Task(ElementID id, Supplier<T> block) {
        this.id = id;
        this.block = block;
    }

    public Task(ElementID id, Runnable block) {
        this.id = id;
        this.block = () -> {
            block.run();
            return null;
        };
    }

    public ElementID id() {
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
