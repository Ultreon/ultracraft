package com.ultreon.craft.network;

public class PacketOverflowException extends PacketException {
    public PacketOverflowException(String type, int read, int max) {
        super("Failed to read '" + type + String.format("', data to read exceeds maximum length: (%s, max %s)", read, max));
    }
}
