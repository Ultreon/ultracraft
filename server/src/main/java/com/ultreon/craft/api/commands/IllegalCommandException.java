package com.ultreon.craft.api.commands;

public class IllegalCommandException extends RuntimeException {
    private final MessageCode code;

    public IllegalCommandException(MessageCode code) {
        super();
        this.code = code;
    }

    public IllegalCommandException(MessageCode code, String message) {
        super(message);
        this.code = code;
    }

    public IllegalCommandException(MessageCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public IllegalCommandException(MessageCode code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public IllegalCommandException(MessageCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public MessageCode getCode() {
        return this.code;
    }
}