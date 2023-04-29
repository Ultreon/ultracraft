package com.ultreon.craft.event;

import com.google.common.reflect.AbstractInvocationHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Deprecated
public final class Event<T> {
    private final Factory<T> factory;
    private final List<T> listeners = new ArrayList<>();

    public Event(Factory<T> factory) {
        this.factory = factory;
    }

    public void listen(T t) {
        this.listeners.add(t);
    }

    public T factory() {
        return factory.create(listeners);
    }

    public interface Factory<T> {
        T create(List<T> listeners);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Event<T> create(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("The array shouldn't contain anything!");
        return of((Class<T>) typeGetter.getClass().getComponentType());
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Event<T> of(Class<T> clazz) {
        return new Event<>(listeners -> (T) Proxy.newProxyInstance(Event.class.getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
                for (T listener : listeners) {
                    invokeMethod(listener, method, args);
                }
                return null;
            }
        }));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Event<T> withResult(T... typeGetter) {
        if (typeGetter.length != 0) throw new IllegalStateException("The array shouldn't contain anything!");
        return withResult((Class<T>) typeGetter.getClass().getComponentType());
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Event<T> withResult(Class<T> clazz) {
        return new Event<>(listeners -> (T) Proxy.newProxyInstance(Event.class.getClassLoader(), new Class[]{clazz}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
                for (T listener : listeners) {
                    EventResult<?> result = Objects.requireNonNull(invokeMethod(listener, method, args));
                    if (result.isInterrupted()) {
                        return result;
                    }
                }
                return EventResult.pass();
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private static <T, R> R invokeMethod(T listener, Method method, Object[] args) throws Throwable {
        return (R) MethodHandles.lookup().unreflect(method)
                .bindTo(listener).invokeWithArguments(args);
    }
}
