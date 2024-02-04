package com.ultreon.craft.api.commands;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface CommandParameter {

    String getComment();

    String getTag();

    boolean isOptional();

    class Text implements CommandParameter {
        private final String[] text;
        boolean isOptional;

        public Text(String[] text, boolean isOptional) {
            this.text = text;
            this.isOptional = isOptional;
        }

        @Override
        public String getComment() {
            return null;
        }

        @Override
        public String getTag() {
            return null;
        }

        @Override
        public boolean isOptional() {
            return this.isOptional;
        }

        @Override
        public String toString() {
            if (this.text.length == 1) {
                return this.text[0];
            } else {
                return "(" + StringUtils.join(this.text, "|") + ")";
            }
        }

        public String[] getText() {
            return this.text;
        }
    }

    class ArgumentType implements CommandParameter {
        private final CommandParser<?> parser;
        private final CommandTabCompleter completer;
        private final Class<?> type;
        private final String tag;
        boolean isOptional;
        private final @Nullable String comment;

        public ArgumentType(CommandParser<?> parser, CommandTabCompleter completer, Class<?> type, String tag, boolean isOptional, @Nullable String comment) {
            this.parser = parser;
            this.completer = completer;
            this.type = type;
            this.tag = tag;
            this.isOptional = isOptional;
            this.comment = comment;
        }

        @Override
        public @Nullable String getComment() {
            return this.comment;
        }

        @Override
        public String getTag() {
            return this.tag;
        }

        @Override
        public boolean isOptional() {
            return this.isOptional;
        }

        @Override
        public String toString() {
            return "<" + this.tag + ">";
        }

        public CommandParser<?> getParser() {
            return this.parser;
        }

        public CommandTabCompleter getCompleter() {
            return this.completer;
        }

        public Class<?> getType() {
            return this.type;
        }
    }

    static CommandParameter ofText(boolean optionalParam, String... text) {
        return new Text(text, optionalParam);
    }

    static CommandParameter ofArgType(CommandParser<?> parser, CommandTabCompleter completer, Class<?> type, String tag, boolean optionalParam, @Nullable String comment) {
        return new ArgumentType(parser, completer, type, tag, optionalParam, comment);
    }

    default @This CommandParameter ifArgType(Consumer<ArgumentType> consumer) {
        if (this instanceof CommandParameter.ArgumentType argType) {
            consumer.accept(argType);
        }
        return this;
    }

    default @This CommandParameter ifText(Consumer<String[]> consumer) {
        if (this instanceof CommandParameter.Text text) {
            consumer.accept(text.text);
        }
        return this;
    }
}