package com.ultreon.craft.util;

public class EncryptionException extends RuntimeException {
    public EncryptionException(Exception e) {
        super("Encryption error", e);
    }
}
