package com.ultreon.craft.network.api;

import net.fabricmc.api.EnvType;

public enum PacketDestination {
    SERVER, CLIENT;

    public PacketDestination opposite() {
        return switch (this) {
            case SERVER -> PacketDestination.CLIENT;
            case CLIENT -> PacketDestination.SERVER;
        };
    }

    public EnvType getSourceEnv() {
        return switch (this) {
            case SERVER -> EnvType.CLIENT;
            case CLIENT -> EnvType.SERVER;
        };
    }

    public EnvType getDestinationEnv() {
        return switch (this) {
            case SERVER -> EnvType.SERVER;
            case CLIENT -> EnvType.CLIENT;
        };
    }
}
