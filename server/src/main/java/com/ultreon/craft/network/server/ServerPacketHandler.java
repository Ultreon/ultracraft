package com.ultreon.craft.network.server;

import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.api.PacketDestination;

public interface ServerPacketHandler extends PacketHandler {
    @Override
    default PacketDestination destination() {
        return PacketDestination.SERVER;
    }
}
