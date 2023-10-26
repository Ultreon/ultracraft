package com.ultreon.craft.client.util.exceptions;

public class ValueMismatchException extends RuntimeException {
    public ValueMismatchException() {
        super();
    }

    public ValueMismatchException(String message) {
        super(message);
    }

    public ValueMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueMismatchException(Throwable cause) {
        super(cause);
    }
}
