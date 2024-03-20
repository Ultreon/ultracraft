package com.ultreon.craft.api.commands;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Interface for command parameters
 */
public interface CommandParameter {

    /**
     * Get the comment for the parameter
     *
     * @return the comment
     */
    @Nullable String getComment();

    /**
     * Get the tag for the parameter
     *
     * @return the tag
     */
    String getTag();

    /**
     * Check if the parameter is optional
     *
     * @return true if optional, false otherwise
     */
    boolean isOptional();

    /**
     * Represents a literal text argument.
     *
     * @see ArgumentType
     */
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

    /**
     * Represents an argument type.
     *
     * @see Text
     */
    class ArgumentType implements CommandParameter {
        // Fields
        private final CommandParser<?> parser;
        private final CommandTabCompleter completer;
        private final Class<?> type;
        private final String tag;
        boolean isOptional;
        private final @Nullable String comment;

        /**
         * Constructor for ArgumentType class.
         *
         * @param parser     the command parser
         * @param completer  the command tab completer
         * @param type       the class type
         * @param tag        the tag string
         * @param isOptional flag indicating if the argument is optional
         * @param comment    additional comment for the argument
         */
        public ArgumentType(CommandParser<?> parser, CommandTabCompleter completer, Class<?> type, String tag, boolean isOptional, @Nullable String comment) {
            this.parser = parser;
            this.completer = completer;
            this.type = type;
            this.tag = tag;
            this.isOptional = isOptional;
            this.comment = comment;
        }

        /**
         * Get the additional comment for the argument.
         *
         * @return the comment or {@code null} if there is no comment
         */
        @Override
        public @Nullable String getComment() {
            return this.comment;
        }

        /**
         * Get the tag string of the argument.
         *
         * @return the tag
         */
        @Override
        public String getTag() {
            return this.tag;
        }

        /**
         * Check if the argument is optional.
         *
         * @return {@code true} if the argument is optional, {@code false} otherwise
         */
        @Override
        public boolean isOptional() {
            return this.isOptional;
        }

        /**
         * Get a string representation of the ArgumentType.
         *
         * @return the string representation
         */
        @Override
        public String toString() {
            return "<" + this.tag + ">";
        }

        /**
         * Get the command parser.
         *
         * @return the command parser
         */
        public CommandParser<?> getParser() {
            return this.parser;
        }

        /**
         * Get the command tab completer.
         *
         * @return the command tab completer
         */
        public CommandTabCompleter getCompleter() {
            return this.completer;
        }

        /**
         * Get the class type of the argument.
         *
         * @return the class type
         */
        public Class<?> getType() {
            return this.type;
        }
    }

    /**
     * Create a CommandParameter with the provided text.
     *
     * @param optionalParam whether the parameter is optional
     * @param text          the text of the parameter
     * @return the created Text CommandParameter
     */
    static CommandParameter ofText(boolean optionalParam, String... text) {
        return new Text(text, optionalParam);
    }

    /**
     * Create a CommandParameter with the provided argument type.
     *
     * @param parser        the parser to use for the argument
     * @param completer     the tab completer to use for the argument
     * @param type          the type of the argument
     * @param tag           the tag for the argument
     * @param optionalParam whether the parameter is optional
     * @param comment       optional comment for the argument
     * @return the created ArgumentType CommandParameter
     */
    static CommandParameter ofArgType(CommandParser<?> parser, CommandTabCompleter completer, Class<?> type, String tag, boolean optionalParam, @Nullable String comment) {
        return new ArgumentType(parser, completer, type, tag, optionalParam, comment);
    }

    /**
     * Perform an action on the ArgumentType if the CommandParameter is an ArgumentType.
     *
     * @param consumer the action to perform on the ArgumentType
     * @return the CommandParameter
     */
    default @This CommandParameter ifArgType(Consumer<ArgumentType> consumer) {
        if (this instanceof CommandParameter.ArgumentType argType) {
            consumer.accept(argType);
        }
        return this;
    }

    /**
     * Perform an action on the Text if the CommandParameter is a Text.
     *
     * @param consumer the action to perform on the Text
     * @return the CommandParameter
     */
    default @This CommandParameter ifText(Consumer<String[]> consumer) {
        if (this instanceof CommandParameter.Text text) {
            consumer.accept(text.text);
        }
        return this;
    }
}