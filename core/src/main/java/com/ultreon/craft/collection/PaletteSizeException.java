package com.ultreon.craft.collection;

public class PaletteSizeException extends IllegalArgumentException {
    public PaletteSizeException() {
        super();
    }

    public PaletteSizeException(String message) {
        super(message);
    }

    public PaletteSizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaletteSizeException(Throwable cause) {
        super(cause);
    }
}
