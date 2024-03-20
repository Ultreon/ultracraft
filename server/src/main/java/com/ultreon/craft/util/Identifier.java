package com.ultreon.craft.util;

import com.ultreon.craft.CommonConstants;
import com.ultreon.libs.commons.v0.exceptions.SyntaxException;
import com.ultreon.libs.commons.v0.tuple.Pair;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * An identifier for an object in the game.
 * The identifier consists of a namespace and a path.
 * This class is immutable and thread-safe.
 * <p>
 * Parsing / formatting example:
 * <pre>
 *  Identifier id = Identifier.parse("ultracraft:crate");
 *  Identifier id = Identifier.tryParse("ultracraft:crate");
 *  Identifier id = new Identifier("ultracraft", "crate");
 *  String name = id.toString();
 * </pre>
 *
 * @since 0.1.0
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class Identifier {
    private final @NotNull String namespace;
    private final @NotNull String path;

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

    /**
     * Parses the given name into an Identifier object.
     *
     * @param name the name to be parsed
     * @return the Identifier object
     */
    @NotNull
    @Contract("_ -> new")
    public static Identifier parse(
            @NotNull String name) {
        return new Identifier(name);
    }

    /**
     * Tries to parse the given name into an Identifier.
     *
     * @param name The name to parse. Can be null.
     * @return The parsed Identifier if successful, null otherwise.
     */
    @Nullable
    @Contract("null -> null")
    public static Identifier tryParse(@Nullable String name) {
        // Return null if the name is null
        if (name == null) return null;

        try {
            // Try to create a new Identifier with the given name
            return new Identifier(name);
        } catch (Exception e) {
            // Return null if an exception occurs during parsing
            return null;
        }
    }

    /**
     * Validates the given location string against a specific pattern.
     *
     * @param location The location string to be validated
     * @return The validated location string
     * @throws SyntaxException if the location string is invalid
     */
    @Contract("_ -> param1")
    public static String testNamespace(String location) {
        // Checks if the location matches the specified pattern
        if (!Pattern.matches("([a-z\\d_]+)([.\\-][a-z\\-\\d_]+){0,16}", location)) {
            throw new SyntaxException("Location is invalid: " + location);
        }
        return location;
    }

    /**
     * Validates and returns the input path.
     *
     * @param path The path to be validated
     * @return The validated path
     * @throws SyntaxException If the path is invalid
     */
    @Contract("_ -> param1")
    public static @NotNull String testPath(String path) {
        // Validate the path against a specific pattern
        if (!Pattern.matches("([a-z_.\\d]+)(/[a-z_.\\d]+){0,16}", path)) {
            throw new SyntaxException("Path is invalid: " + path);
        }

        return path;
    }

    /**
     * This method checks if two Identifiers are equal.
     *
     * @param o The object to compare with this Identifier.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        // Check if the objects are the same instance
        if (this == o) {
            return true;
        }

        // Check if the objects are null or not of the same class
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        // Cast the object to Identifier type
        Identifier that = (Identifier) o;

        // Check if the namespaces and paths of the Identifiers are equal
        return this.namespace.equals(that.namespace) && this.path.equals(that.path);
    }

    /**
     * This method calculates the hash code for the object.
     * It combines the hash codes of the namespace and path fields.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {
        // Use Objects.hash() method to calculate the hash code
        return Objects.hash(this.namespace, this.path);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string in the format "namespace:path"
     */
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

    /**
     * Returns a new Identifier with the provided namespace.
     *
     * @param namespace the new namespace to use
     * @return a new Identifier with the updated namespace
     */
    @Contract("_ -> new")
    public Identifier withNamespace(String namespace) {
        return new Identifier(namespace, this.path);
    }

    /**
     * Returns a new Identifier with the provided path.
     *
     * @param path the new path to use
     * @return a new Identifier with the updated path
     */
    @Contract("_ -> new")
    public Identifier withPath(String path) {
        return new Identifier(this.namespace, path);
    }

    /**
     * Maps the location using the provided UnaryOperator.
     *
     * @param location the UnaryOperator to map the location
     * @return a new Identifier with the mapped location
     */
    @Contract("_ -> new")
    public Identifier mapLocation(UnaryOperator<String> location) {
        return new Identifier(location.apply(this.namespace), this.path);
    }

    /**
     * Maps the path using the provided UnaryOperator.
     *
     * @param path the UnaryOperator to map the path
     * @return a new Identifier with the mapped path
     */
    @Contract("_ -> new")
    public Identifier mapPath(UnaryOperator<String> path) {
        return new Identifier(this.namespace, path.apply(this.path));
    }

    /**
     * Maps both the path and location using the provided UnaryOperators.
     *
     * @param path the UnaryOperator to map the path
     * @param location the UnaryOperator to map the location
     * @return a new Identifier with the mapped path and location
     */
    @Contract("_, _ -> new")
    public Identifier map(UnaryOperator<String> path, UnaryOperator<String> location) {
        return new Identifier(location.apply(this.namespace), path.apply(this.path));
    }

    /**
     * Reduce the namespace and path using the provided function.
     *
     * @param func the function to apply to the namespace and path
     * @param <T> the type of the result
     * @return the result of applying the function to the namespace and path
     */
    public <T> T reduce(BiFunction<String, String, T> func) {
        return func.apply(this.namespace, this.path);
    }

    /**
     * Returns the list representation of the namespace and path.
     *
     * @return A list containing the namespace and path.
     */
    @NotNull
    @Unmodifiable
    @Contract(value = "-> new", pure = true)
    public List<String> toList() {
        return Arrays.asList(this.namespace, this.path);
    }

    /**
     * Converts the namespace and path to an ArrayList of strings.
     *
     * @return ArrayList of strings containing the namespace and path.
     */
    @NotNull
    @Contract(" -> new")
    public ArrayList<String> toArrayList() {
        // Create a new ArrayList to store the namespace and path
        ArrayList<String> list = new ArrayList<>();

        // Add the namespace and path to the list
        list.add(this.namespace);
        list.add(this.path);

        // Return the list containing the namespace and path
        return list;
    }

    /**
     * Returns an unmodifiable view of the collection as a list of strings.
     *
     * @return unmodifiable view of the collection as a list of strings
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Collection<String> toCollection() {
        return this.toList();
    }

    /**
     * Converts the namespace and path to a Pair of strings.
     *
     * @return a Pair of strings representing the namespace and path
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public Pair<String, String> toPair() {
        return new Pair<>(this.namespace, this.path);
    }

    /**
     * Converts the namespace and path to an array of strings.
     *
     * @return an array of strings representing the namespace and path
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public String[] toArray() {
        return new String[]{this.namespace, this.path};
    }
}
