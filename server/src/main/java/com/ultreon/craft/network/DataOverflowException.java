package com.ultreon.craft.network;

public class DataOverflowException extends PacketException {
    public DataOverflowException(String type, int write, int max) {
        super("Failed to write '" + type + String.format("', data to write exceeds maximum length: (%s, max %s)", write, max));
    }
}
