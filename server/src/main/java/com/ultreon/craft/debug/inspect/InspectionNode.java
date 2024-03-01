package com.ultreon.craft.debug.inspect;

import com.google.common.base.Suppliers;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a node in the inspection tree.
 */
@ThreadSafe
public class InspectionNode<T> {
    private final ConcurrentMap<String, InspectionNode<?>> nodes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<String>> elements = new ConcurrentHashMap<>();
    private final String name;
    private final @Nullable InspectionRoot<?> root;
    private final @Nullable InspectionNode<?> parent;
    private final Function<InspectionNode<T>, T> value;
    private final Consumer<InspectionNode<T>> filler;
    private boolean hasFilled;

    /**
     * Constructs a new inspection node.
     *
     * @param name   the name of the inspection node.
     * @param parent the parent of the inspection node.
     * @param root   the inspection root.
     * @param value  the value of the inspection node
     */
    public InspectionNode(String name, @Nullable InspectionNode<?> parent, @Nullable InspectionRoot<?> root,
                          Function<InspectionNode<T>, T> value) {
        this.name = name;
        this.parent = parent;
        this.root = root;
        this.value = value;
        this.filler = n -> {
        };
    }

    public InspectionNode(String name, @Nullable InspectionNode<?> parent, @Nullable InspectionRoot<?> root,
                          Function<InspectionNode<T>, T> value, Consumer<InspectionNode<T>> filler) {
        this.name = name;
        this.parent = parent;
        this.root = root;
        this.value = value;
        this.filler = filler;
    }

    /**
     * Create an element in the inspection node.
     * Uses supplier for dynamic values.
     *
     * @param name  the name of the element.
     * @param value supplier of the element value.
     */
    public void create(String name, Supplier<@Nullable Object> value) {
        this.elements.putIfAbsent(name, () -> {
            try {
                return InspectionRoot.format(value.get());
            } catch (Exception t) {
                return "ERROR";
            }
        });
    }

    public <C> void create(String name, NodeMapping<T, C> value) {
        this.elements.putIfAbsent(name, () -> {
            try {
                return InspectionRoot.format(value.map(this.value.apply(this)));
            } catch (Exception t) {
                return "ERROR";
            }
        });
    }

    /**
     * Remove a node from the inspection node.
     *
     * @param name the name of the node to removeFirst.
     * @return true if the element was present, false otherwise.
     */
    @CanIgnoreReturnValue
    public boolean remove(String name) {
        return this.elements.remove(name) != null;
    }

    @SafeVarargs
    @CanIgnoreReturnValue
    public final <C> InspectionNode<C> createNode(String name, Supplier<C> value, C... typeGetter) {
        return this.createNode0(name, typeGetter, Suppliers.memoizeWithExpiration(value::get, 2, TimeUnit.SECONDS));
    }

    @SafeVarargs
    @CanIgnoreReturnValue
    public final <C> InspectionNode<C> createNode(String name, NodeMapping<T, C> value, C... typeGetter) {
        return this.createNode0(name, typeGetter, Suppliers.memoizeWithExpiration(() -> {
            Function<InspectionNode<T>, T> v = this.value;
            return value.map(v.apply(this));
        }, 2, TimeUnit.SECONDS));
    }

    @NotNull
    private <C> InspectionNode<@Nullable C> createNode0(String name, C[] typeGetter, Supplier<C> val) {
        var node = new InspectionNode<@Nullable C>(name, this, this.getRoot(), n -> val.get(), n -> InspectionRoot.fill(n, typeGetter.getClass().getComponentType()));
        this.nodes.put(name, node);
        return node;
    }

    @CanIgnoreReturnValue
    public @Nullable InspectionNode<?> removeNode(String name) {
        return this.nodes.remove(name);
    }

    public Map<String, InspectionNode<?>> getNodes() {
        if (!this.hasFilled) {
            this.filler.accept(this);
            this.hasFilled = true;
        }
        return this.nodes;
    }

    public Map<String, Supplier<String>> getElements() {
        return this.elements;
    }

    public String getName() {
        return this.name;
    }

    /**
     * @return the parent of the inspection node, or {@code null} if the inspection node is the root.
     */
    public @Nullable InspectionNode<?> getParent() {
        return this.parent;
    }

    /**
     * @return the root of the inspection tree.
     */
    public @Nullable InspectionRoot<?> getRoot() {
        return this.root;
    }

    public T getValue() {
        return this.value.apply(this);
    }

    @FunctionalInterface
    public interface NodeMapping<T, C> {
        @Nullable C map(@NotNull T value);
    }
}
