package com.ultreon.craft.network.client;

import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.api.PacketDestination;

public interface ClientPacketHandler extends PacketHandler {
    @Override
    default PacketDestination destination() {
        return PacketDestination.SERVER;
    }
}
