package com.ultreon.craft;

import com.google.common.collect.Queues;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.ultreon.craft.UltreonCraft.LOGGER;

@SuppressWarnings("NewApi")
public class PollingExecutorService implements ExecutorService {
    private final Queue<Runnable> taskList = Queues.synchronizedQueue(new ArrayDeque<>());
    private final Thread thread;
    private boolean isShutdown = false;
    @Nullable
    private Runnable active;

    PollingExecutorService() {
        this(Thread.currentThread());
    }

    public PollingExecutorService(@NotNull Thread thread) {
        this.thread = thread;
    }

    @Override
    public void shutdown() {
        this.isShutdown = true;
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        this.isShutdown = true;
        return new ArrayList<>(this.taskList);
    }

    @Override
    public boolean isShutdown() {
        return this.isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return this.isShutdown && this.taskList.isEmpty();
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
        this.execute(() -> {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> submit(@NotNull Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.execute(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task) {
                    Identifier id = ((Task) task).id();
                    LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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

        this.taskList.add(command);
    }

    private boolean isSameThread() {
        return Thread.currentThread().getId() == this.thread.getId();
    }

    public void poll() {
        if ((this.active = this.taskList.poll()) != null)
            this.active.run();
    }

    public void pollAll() {
        while ((this.active = this.taskList.poll()) != null)
            this.active.run();
    }
}
