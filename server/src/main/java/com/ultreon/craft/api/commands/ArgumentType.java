package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.error.CommandError;

import java.util.*;
import java.util.regex.Pattern;

public abstract class ArgumentType<T> {
    protected final String id;
    protected int argsNeeded = 1;

    public ArgumentType() {
        if (!ArgumentType.ARGUMENT_ID_MAP.containsKey(this))
            throw new IllegalStateException("ID-Mapping for this argument is not created yet.");
        this.id = ArgumentType.ARGUMENT_ID_MAP.get(this);
    }

    protected ArgumentType(String id) {
        this.id = id;
        if (!Pattern.compile("[a-z\\-]*").matcher(id).find())
            throw new IllegalArgumentException("ID doesn't match the regex: [a-z\\-]*");
        if (ArgumentType.ID_CLASS_MAP.containsKey(id) && !Objects.equals(ArgumentType.ID_CLASS_MAP.get(id), this.getClass().getName()))
            throw new IllegalStateException("Duplicate argument type ID: " + id);
        ArgumentType.ID_ARGUMENT_MAP.put(id, this);
        ArgumentType.ARGUMENT_ID_MAP.put(this, id);
        ArgumentType.ID_CLASS_MAP.put(id, this.getClass().getName());
    }

    public abstract Class<? extends Argument<?>> getArgClass();

    public Class<?>[] getArgTypeClasses() {
        return new Class<?>[0];
    }

    public abstract Result<? extends Argument<? extends T>> parse(
            CommandSender var1, CommandContext var2, String tag, String[] var3);

    protected abstract List<String> tabComplete(CommandSender var1, CommandContext var2, String[] var3);

    protected abstract boolean isValid(CommandSender var1, CommandContext var2, String[] var3);

    public int getArgsNeeded(CommandSender sender, CommandContext commandCtx, String[] args) {
        return this.argsNeeded;
    }

    @Override
    public String toString() {
        if (!ArgumentType.ID_ARGUMENT_MAP.containsKey(this.id))
            throw new IllegalStateException("Missing required id -> argument mapping.");
        if (!ArgumentType.ARGUMENT_ID_MAP.containsValue(this.id))
            throw new IllegalStateException("Missing required argument -> id mapping.");
        return "ArgumentType[" + this.id + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        ArgumentType<?> argument = (ArgumentType<?>) other;
        return Objects.equals(this.id, argument.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public static class Result<T> {
        private final T object;
        private final CommandError error;

        public Result(T object) {
            this(object, null);
        }

        public Result(T object, CommandError error) {
            this.object = object;
            this.error = error;
        }

        public Result(CommandError error) {
            this(null, error);
        }

         public T get() {
            return this.object;
        }

         public boolean hasError() {
            return this.error != null;
        }
    }

    private static final Map<String, ArgumentType<?>> ID_ARGUMENT_MAP = new HashMap<>();
    private static final Map<ArgumentType<?>, String> ARGUMENT_ID_MAP = new HashMap<>();
    private static final Map<String, String> ID_CLASS_MAP = new HashMap<>();

    static <T extends ArgumentType<?>> T register(T argumentType) {
        return argumentType;
    }

    static Map<String, ArgumentType<?>> getIdArgumentMap() {
        return Collections.unmodifiableMap(ArgumentType.ID_ARGUMENT_MAP);
    }

    static Map<ArgumentType<?>, String> getArgumentIdMap() {
        return Collections.unmodifiableMap(ArgumentType.ARGUMENT_ID_MAP);
    }
}