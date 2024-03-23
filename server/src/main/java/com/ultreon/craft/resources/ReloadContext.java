package com.ultreon.craft.resources;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.util.PollingExecutorService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class ReloadContext implements Disposable {
    private final PollingExecutorService executor;
    private final List<CompletableFuture<?>> futures = new ArrayList<>();
    private final ResourceManager resourceManager;

    public ReloadContext(PollingExecutorService executor, ResourceManager resourceManager) {
        this.executor = executor;
        this.resourceManager = resourceManager;
    }

    public static ReloadContext create(PollingExecutorService executor, ResourceManager resourceManager) {
        return new ReloadContext(executor, resourceManager);
    }

    public void submit(Runnable submission) {
        CompletableFuture<Void> submitted = this.executor.submit(submission);
        futures.add(submitted);
    }

    public @NotNull <T> CompletableFuture<T> submit(Callable<T> submission) {
        CompletableFuture<T> submitted = this.executor.submit(submission);
        futures.add(submitted);
        return submitted;
    }

    public boolean isDone() {
        return futures.stream().allMatch(CompletableFuture::isDone);
    }

    public void dispose() {
        if (!isDone()) {
            throw new IllegalStateException("Cannot dispose when not done");
        }

        this.futures.clear();
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }
}
