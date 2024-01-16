package com.ultreon.xeox.loader;

import java.io.IOException;

public class ModImportException extends IOException {
    public ModImportException() {

    }

    public ModImportException(String message) {
        super(message);
    }

    public ModImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModImportException(Throwable cause) {
        super(cause);
    }
}
