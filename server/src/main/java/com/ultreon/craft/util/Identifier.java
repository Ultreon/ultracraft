package com.ultreon.craft.util;

import com.ultreon.craft.CommonConstants;
import com.ultreon.libs.commons.v0.exceptions.SyntaxException;
import com.ultreon.libs.commons.v0.tuple.Pair;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class Identifier {
    private final @NotNull String namespace;
    private final @NotNull String path;

    /**
     * Sets the default namespace to use.
     *
     * @param ignoredDefaultNamespace the default namespace to use.
     * @deprecated Setting default namespace is deprecated and will be removed in the future.
     */
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static synchronized void setDefaultNamespace(String ignoredDefaultNamespace) {
        CommonConstants.LOGGER.warn("Setting default namespace is deprecated and will be removed in the future.");
    }

    /**
     * @return the default namespace.
     * @deprecated Use {@link CommonConstants#NAMESPACE} instead.
     */
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static String getDefaultNamespace() {
        return CommonConstants.NAMESPACE;
    }

    public Identifier(@NotNull String namespace, @NotNull String path) {
        testNamespace(namespace);
        testPath(path);

        this.namespace = namespace;
        this.path = path;
    }

    public Identifier(@NotNull String name) {
        String[] split = name.split(":", 2);
        if (split.length == 2) {
            this.namespace = testNamespace(split[0]);
            this.path = testPath(split[1]);
        } else {
            this.namespace = CommonConstants.NAMESPACE;
            this.path = testPath(name);
        }
    }

    @NotNull
    @Contract("_ -> new")
    public static Identifier parse(
            @NotNull String name) {
        return new Identifier(name);
    }

    @Nullable
    @Contract("null -> null")
    public static Identifier tryParse(@Nullable String name) {
        if (name == null) return null;

        try {
            return new Identifier(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Contract("_ -> param1")
    public static String testNamespace(String location) {
        if (!Pattern.matches("([a-z\\d_]+)([.\\-][a-z\\-\\d_]+){0,16}", location)) {
            throw new SyntaxException("Location is invalid: " + location);
        }
        return location;
    }

    @Contract("_ -> param1")
    public static @NotNull String testPath(String path) {
        if (!Pattern.matches("([a-z_.\\d]+)(/[a-z_.\\d]+){0,16}", path)) {
            throw new SyntaxException("Path is invalid: " + path);
        }
        return path;
    }

    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return this.namespace.equals(that.namespace) && this.path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.path);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        return this.namespace + ":" + this.path;
    }

    /**
     * @return object location (the mod id / namespace).
     */
    @NotNull
    @Contract(pure = true)
    public String namespace() {
        return this.namespace;
    }

    /**
     * @return object path.
     */
    @NotNull
    @Contract(pure = true)
    public String path() {
        return this.path;
    }

    @Contract("_ -> new")
    public Identifier withNamespace(String namespace) {
        return new Identifier(namespace, this.path);
    }

    @Contract("_ -> new")
    public Identifier withPath(String path) {
        return new Identifier(this.namespace, path);
    }

    @Contract("_ -> new")
    public Identifier mapLocation(UnaryOperator<String> location) {
        return new Identifier(location.apply(this.namespace), this.path);
    }

    @Contract("_ -> new")
    public Identifier mapPath(UnaryOperator<String> path) {
        return new Identifier(this.namespace, path.apply(this.path));
    }

    @Contract("_, _ -> new")
    public Identifier map(UnaryOperator<String> path, UnaryOperator<String> location) {
        return new Identifier(location.apply(this.namespace), path.apply(this.path));
    }

    public <T> T reduce(BiFunction<String, String, T> func) {
        return func.apply(this.namespace, this.path);
    }

    @NotNull
    @Unmodifiable
    @Contract(value = "-> new", pure = true)
    public List<String> toList() {
        return Arrays.asList(this.namespace, this.path);
    }

    @NotNull
    @Contract(" -> new")
    public ArrayList<String> toArrayList() {
        ArrayList<String> list = new ArrayList<>();
        list.add(this.namespace);
        list.add(this.path);
        return list;
    }

    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Collection<String> toCollection() {
        return this.toList();
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public Pair<String, String> toPair() {
        return new Pair<>(this.namespace, this.path);
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public String[] toArray() {
        return new String[]{this.namespace, this.path};
    }
}
