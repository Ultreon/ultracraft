package com.ultreon.craft.server.util;

public class TimeFormatException extends IllegalArgumentException {
    public TimeFormatException() {
        super();
    }

    public TimeFormatException(String s) {
        super(s);
    }

    public TimeFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeFormatException(Throwable cause) {
        super(cause);
    }
}
