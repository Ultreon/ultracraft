package com.ultreon.craft.api.commands.selector;

import com.ultreon.craft.api.commands.error.CommandError;
import com.ultreon.craft.api.commands.error.InvalidSelectorError;
import com.ultreon.craft.api.commands.error.SelectorTooSmallError;
import org.jetbrains.annotations.Nullable;

public abstract class BaseSelector<T> {
    protected SelectorKey key;
    protected String stringValue;
    protected CommandError error;
    protected Result<T> result;

    public BaseSelector(Parsed parsed) {
        this.error = parsed.error();
        this.key = parsed.key();
        this.stringValue = parsed.value();
    }

    public BaseSelector(String text) {
        this(BaseSelector.parseSelector(text));
    }

    public SelectorKey getKey() {
        return this.key;
    }

    protected abstract Result<T> calculateData();

    public String getStringValue() {
        return this.stringValue;
    }

    public @Nullable T getValue() {
        return this.result.value();
    }

    public boolean hasError() {
        return this.result.hasError();
    }

    public CommandError getError(){
        return this.result.error();
    }

    public record Result<T>(@Nullable T value, @Nullable CommandError error) {

        public boolean hasError() {
                return this.error != null;
            }
        }

    public record Parsed(SelectorKey key, String value, CommandError error) {

        public boolean hasError() {
                return this.error != null;
            }

            @Override
            public String toString() {
                return this.key.toString() + this.value;
            }
        }

    @Override
    public String toString() {
        return this.key.symbol() + this.stringValue;
    }

    public static Parsed parseSelector(String text) throws IllegalArgumentException {
        if (text.length() <= 1) {
            return new Parsed(null, null, new SelectorTooSmallError(text));
        }
        SelectorKey key = SelectorKey.fromKey(text.charAt(0));
        if (key == null) return new Parsed(null, null, new InvalidSelectorError(text));
        return new Parsed(key, text.substring(1), null);
    }

    public static Parsed parseSelector(String text, Parsed def) {
        if (text.length() <= 1) {
            return null;
        }
        SelectorKey key = SelectorKey.fromKey(text.charAt(0));
        if (key == null) return def;
        return new Parsed(key, text.substring(1), null);
    }

}