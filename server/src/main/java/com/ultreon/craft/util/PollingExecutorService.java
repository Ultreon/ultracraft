package com.ultreon.craft.util;

import com.google.common.collect.Queues;
import com.ultreon.craft.debug.Profiler;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SuppressWarnings("NewApi")
public class PollingExecutorService implements ExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger("PollingExecutorService");
    private final Queue<Runnable> tasks = Queues.synchronizedQueue(new ArrayDeque<>());
    protected Thread thread;
    private boolean isShutdown = false;
    @Nullable
    private Runnable active;
    public final Profiler profiler;

    public PollingExecutorService(Profiler profiler) {
        this(Thread.currentThread(), profiler);
    }

    public PollingExecutorService(@NotNull Thread thread, Profiler profiler) {
        this.thread = thread;
        this.profiler = profiler;
    }

    @Override
    public void shutdown() {
        this.isShutdown = true;
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        this.isShutdown = true;
        return List.copyOf(this.tasks);
    }

    @Override
    public boolean isShutdown() {
        return this.isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return this.isShutdown && this.tasks.isEmpty();
    }

    @Override
    @SuppressWarnings("BusyWait")
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        var endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        while (!this.isTerminated() && System.currentTimeMillis() < endTime) Thread.sleep(100);
        return this.isTerminated();
    }

    @Override
    public <T> @NotNull CompletableFuture<T> submit(@NotNull Callable<T> task) {
        var future = new CompletableFuture<T>();
        if (this.isSameThread()) {
            try {
                future.complete(task.call());
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        this.execute(() -> {
            try {
                var result = task.call();
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    @Override
    public <T> @NotNull CompletableFuture<T> submit(@NotNull Runnable task, T result) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (this.isSameThread()) {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        this.execute(() -> {
            this.profiler.section(task.getClass().getName(), () -> {
                try {
                    task.run();
                    future.complete(result);
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
        });
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> submit(@NotNull Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (this.isSameThread()) {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        this.execute(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    PollingExecutorService.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();
        return futures.stream()
                .map(CompletableFuture::join)
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toList());
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();
        List<Future<T>> resultList = new ArrayList<>();

        for (CompletableFuture<T> future : futures) {
            long timeLeft = endTime - System.currentTimeMillis();
            if (timeLeft <= 0)
                break;

            resultList.add(future.orTimeout(timeLeft, TimeUnit.MILLISECONDS).toCompletableFuture());
        }

        return resultList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        var futures = tasks.stream()
                .map(this::submit)
                .toList();

        try {
            return CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(o -> (T)o)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        var endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        var futures = tasks.stream()
                .map(this::submit)
                .toList();

        try {
            var result = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(o -> ((CompletableFuture<T>)o).join());

            var timeLeft = endTime - System.currentTimeMillis();
            if (timeLeft <= 0)
                throw new TimeoutException();

            return result.orTimeout(timeLeft, TimeUnit.MILLISECONDS).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (this.isShutdown)
            throw new RejectedExecutionException("Executor is already shut down");

        if (this.isSameThread()) {
            command.run();
            return;
        }

        this.tasks.add(command);
    }

    private boolean isSameThread() {
        return Thread.currentThread().getId() == this.thread.getId();
    }

    public void poll() {
        if ((this.active = this.tasks.poll()) != null) {
            try {
                this.active.run();
            } catch (Throwable t) {
                PollingExecutorService.LOGGER.error("Failed to run task:", t);
            }
        }
    }

    public void pollAll() {
        while ((this.active = this.tasks.poll()) != null) {
            var task = this.active;

            try {
                task.run();
            } catch (Throwable t) {
                PollingExecutorService.LOGGER.error("Failed to run task:", t);
            }
        }
    }
}
