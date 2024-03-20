package com.ultreon.craft.util;

import org.jetbrains.annotations.Contract;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @param <T>
 * @since 0.2.0
 * @author <a href="https://github.com/XyperCodee">XyperCode</a>
 */
public class Result<T> {
    private final Ok<T> ok;
    private final Failure failure;

    private Result(Ok<T> ok, Failure failure) {
        this.ok = ok;
        this.failure = failure;
    }

    public static <T> Result<T> ok(T left) {
        return new Result<>(new Ok<>(left), null);
    }

    public static <T> Result<T> ok() {
        return new Result<>(new Ok<>(null), null);

    }

    public static <T> Result<T> failure(Throwable right) {
        return new Result<>(null, new Failure(right));
    }

    public T getOk() {
        if (this.ok == null) throw new NoSuchElementException("The value is not present.");
        return this.ok.value;
    }

    public Throwable getFailure() {
        if (this.failure == null) throw new NoSuchElementException("The failure is not present.");
        return this.failure.throwable;
    }

    public boolean isValuePresent() {
        return this.ok != null;
    }

    public boolean isFailurePresent() {
        return this.failure != null;
    }

    public void ifValue(Consumer<T> onValue) {
        if (this.ok != null) onValue.accept(this.ok.value);
    }

    public void ifFailure(Consumer<Throwable> onFailure) {
        if (this.failure != null) onFailure.accept(this.failure.throwable);
    }

    public void ifValueOrElse(Consumer<T> onValue, Runnable runnable) {
        if (this.ok != null) onValue.accept(this.ok.value);
        else runnable.run();
    }

    public void ifFailureOrElse(Consumer<Throwable> onFailure, Runnable runnable) {
        if (this.failure != null) onFailure.accept(this.failure.throwable);
        else runnable.run();
    }

    public T getValueOrNull() {
        return this.ok.value;
    }

    public Throwable getFailureOrNull() {
        return this.failure.throwable;
    }

    @Contract("!null -> !null")
    public T getValueOr(T other) {
        Ok<T> ok = this.ok;
        if (ok == null) return other;
        T value = ok.value;
        return value == null ? other : value;
    }

    public Throwable getFailureOr(Throwable other) {
        Failure failure = this.failure;
        if (failure == null) return other;
        Throwable value = failure.throwable;
        return value == null ? other : value;
    }

    public T getValueOrGet(Supplier<? extends T> other) {
        Ok<T> ok = this.ok;
        if (ok == null) return other.get();
        T value = ok.value;
        return value == null ? other.get() : value;
    }

    public Throwable getFailureOrGet(Supplier<? extends Throwable> other) {
        Failure failure = this.failure;
        if (failure == null) return other.get();
        Throwable value = failure.throwable;
        return value == null ? other.get() : value;
    }

    public void ifAny(Consumer<T> onValue, Consumer<Throwable> onFailure) {
        if (this.ok != null) onValue.accept(this.ok.value);
        else if (this.failure != null) onFailure.accept(this.failure.throwable);
    }

    private static class Ok<L> {
        private final L value;

        public Ok(L value) {
            this.value = value;
        }
    }

    private static class Failure {
        private final Throwable throwable;

        public Failure(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
