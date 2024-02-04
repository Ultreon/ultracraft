package com.ultreon.craft.api.commands.perms;

import org.jetbrains.annotations.NotNull;

public class Permission {
    private final String key;
    private final boolean extend;
    private final boolean all;

    public Permission(@NotNull String key) {
        if (key.endsWith(".")) throw new IllegalArgumentException("Permission key should not end with a period.");
        if (key.startsWith(".")) throw new IllegalArgumentException("Permission key should not start with a period.");
        this.all = key.equals("*");
        this.extend = key.endsWith(".*");
        this.key = this.extend ? key.substring(0, key.length() - 2) : key;
    }

    public boolean allows(Permission permission) {
        if (this.all) return true;

        if (this.extend && (this.key + ".").startsWith(permission.key)) {
            return true;
        }

        return permission.key.equals(this.key);
    }

    public String[] getParts() {
        return this.key.split("\\.");
    }

    public String getKey() {
        return this.key;
    }

    public String toString() {
        return this.extend ? this.key + ".*" : this.key;
    }
}
