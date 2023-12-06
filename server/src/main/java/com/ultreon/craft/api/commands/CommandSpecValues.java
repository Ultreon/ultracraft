package com.ultreon.craft.api.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CommandSpecValues extends HashSet<String> {

    public CommandSpecValues() {
        super();
    }

    public CommandSpecValues(Set<String> values) {
        super(values);
        this.removeIf(String::isEmpty);
    }

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