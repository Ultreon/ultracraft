package com.ultreon.craft.resources;

import com.ultreon.craft.UltreonCraft;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Identifier {
    private static final String DEFAULT_NAMESPACE = UltreonCraft.NAMESPACE;
    private static final Pattern namespaceRegex = Pattern.compile("[a-z0-9_\\-]*");
    private static final Pattern pathRegex = Pattern.compile("[a-z0-9_\\-/.]*");
    private final String namespace;
    private final String path;

    public Identifier(String text) {
        if (text.contains(":")) {
            var split = text.split(":", 2);
            namespace = testNamespace(split[0]);
            path = testPath(split[1]);
        } else {
            namespace = DEFAULT_NAMESPACE;
            path = testPath(text);
        }
    }

    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    private String testNamespace(String namespace) {
        if (namespaceRegex.matcher(namespace).find()) {
            return namespace;
        }

        throw new IllegalArgumentException("Invalid namespace: $namespace");
    }

    private String testPath(String path) {
        if (namespaceRegex.matcher(path).find()) {
            return path;
        }

        throw new IllegalArgumentException("Invalid path: $path");
    }

    public String namespace() {
        return namespace;
    }

    public String path() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Identifier) obj;
        return Objects.equals(this.namespace, that.namespace) &&
                Objects.equals(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }

    @Override
    public String toString() {
        return namespace + "@" + path;
    }
}
