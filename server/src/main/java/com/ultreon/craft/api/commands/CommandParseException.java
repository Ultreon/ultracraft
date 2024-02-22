package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.error.CommandError;
import com.ultreon.craft.server.chat.Chat;

public class CommandParseException extends Exception {
    private final String originalMessage;
    private final int offset;

    public CommandParseException(String originalMessage) {
        super(originalMessage);
        this.originalMessage = originalMessage;
        this.offset = -1;
    }

    public CommandParseException(String originalMessage, int offset) {
        super(originalMessage + (offset >= 0 ? " (at column " + (offset + 1) + ")" : ""));
        this.originalMessage = originalMessage;
        this.offset = offset;
    }

    public CommandParseException(CommandError error, int offset) {
        this(error.getMessage(), offset);
    }

    public void send(CommandSender sender) {
        this.sendErrors(sender, CommandReader.get());
    }

    private void sendErrors(CommandSender sender, CommandReader ctx) {
        String command = ctx.getCommandlineArgs().substring(0, ctx.getOffset());
        if (command.length() > 15) {
            command = "... " + command.substring(command.length() - 13, command.length());
        }
        Chat.sendError(sender, this.originalMessage);
        Chat.sendError(sender, "<gray>  /" + ctx.getCommand() + " " + command + " <red>&<-- HERE");
    }

    public static class NotADigit extends CommandParseException {
        public NotADigit(char got) {
            super("Expected a digit, but got: " + got);
        }

        public NotADigit(char got, int offset) {
            super("Expected a digit, but got: " + got, offset);
        }
    }

    public static class NotANumber extends CommandParseException {
        public NotANumber(String got) {
            super("Expected to find a number, but got: " + got);
        }

        public NotANumber(String got, int offset) {
            super("Expected to find a number, but got: " + got, offset);
        }
    }

    public static class EndOfCommand extends CommandParseException {
        public EndOfCommand(int index) {
            super("Expected to find another argument but was at the end of command.", index);
        }
    }

    public static class EndOfArgument extends CommandParseException {
        public EndOfArgument(int index) {
            super("The reader is at the end of argument, can't proceed.", index);
        }
    }

    public static class NotAtEndOfArg extends CommandParseException {
        public NotAtEndOfArg(int index) {
          super("Not at the end of an argument.", index);
        }
    }

    public static class NotAtStartOfArg extends CommandParseException {
        public NotAtStartOfArg(int index) {
            super("Expected another command argument.", index);
        }
    }

	public static class NotFound extends CommandParseException {
        private final Object value;

        public NotFound(String type, int offset) {
            super("That " + type + " was not found", offset);
            this.value = null;
        }

        public NotFound(String type, Object value, int offset) {
            super("The " + type + " named '" + NotFound.repr(value) + "' was not found", offset);
            this.value = value;
        }

        private static String repr(Object value) {
            return value.toString()
                .replace("\\\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\u000c", "\\u000c")
                .replace("'", "\\'");
        }
    }

	public static class Invalid extends CommandParseException {
        private final Object value;

        public Invalid(String type, int offset) {
            super("That " + type + " is invalid", offset);
            this.value = null;
        }

        public Invalid(String type, Object value, int offset) {
            super("The " + type + " named '" + Invalid.repr(value) + "' is invalid", offset);
            this.value = value;
        }

        private static String repr(Object value) {
            return value.toString()
                .replace("\\\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\u000c", "\\u000c")
                .replace("'", "\\'");
        }
    }
}