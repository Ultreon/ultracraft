package com.ultreon.craft.api.commands;

import java.lang.reflect.Method;

public class InvalidCommandMethodError extends Error {
    public InvalidCommandMethodError(Method method) {
        super("Invalid command method: %s in class: %s".formatted(method.getName(), method.getDeclaringClass().getName()));
    }

    public InvalidCommandMethodError(Method method, Throwable cause) {
        super("Invalid command method: %s in class: %s".formatted(method.getName(), method.getDeclaringClass().getName()), cause);
    }

    public InvalidCommandMethodError(String message) {
        super(message);
    }

    public InvalidCommandMethodError(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCommandMethodError(Throwable cause) {
        super(cause);
    }
}
