package com.ultreon.craft.api.commands;

public class SpecSyntaxException extends RuntimeException {
    private final String message;
    private final int column;
    private final int line;

    public SpecSyntaxException(int line, int column, String message) {
        super("Line " + line + ", column " + column + ": " + message);
        this.line = line;
        this.column = column;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "Line " + line() + ", column " + column() + ": " + this.message;
    }

    private int column() {
        return this.column;
    }

    private int line() {
        return this.line;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
