package com.ultreon.craft.network;

import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.Packet;

public interface PacketHandler {
    PacketDestination destination();

    void onDisconnect(String message);

    boolean isAcceptingPackets();

    default boolean shouldHandlePacket(Packet<?> packet) {
        return this.isAcceptingPackets();
    }

    PacketContext context();

    default boolean isAsync() {
        return true;
    }
}
