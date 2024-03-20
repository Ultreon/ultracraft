package com.ultreon.craft.api.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Custom HashSet implementation for storing command specification values.
 */
public class CommandSpecValues extends HashSet<String> {
    /**
     * Constructs an empty CommandSpecValues with an initial capacity sufficient to hold the specified number of elements.
     */
    public CommandSpecValues() {
        super();
    }

    /**
     * Constructs a CommandSpecValues with the elements of the specified collection.
     * Removes any empty strings from the collection.
     *
     * @param values the collection whose elements are to be placed into this CommandSpecValues.
     */
    public CommandSpecValues(Set<String> values) {
        super(values);
        this.removeIf(String::isEmpty);
    }

    /**
     * Adds the specified element to this set if it is a valid command spec value.
     *
     * @param element the element to add to the set
     * @return true if this set did not already contain the specified element
     * @throws IllegalArgumentException if the element is not a valid command spec value
     */
    @Override
    public boolean add(String element) {
        if (element.isEmpty()) {
            return false;
        }

        if (Pattern.matches("[a-z0-9\\-]+", element)) {
            return super.add(element);
        } else {
            throw new IllegalArgumentException("Command spec value is invalid, only lowercase letters, numbers and dashes allowed.");
        }
    }
}