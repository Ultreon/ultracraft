package com.ultreon.craft.network.client;

import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.libs.commons.v0.Identifier;

public interface ClientPacketHandler extends PacketHandler {
    @Override
    default PacketDestination destination() {
        return PacketDestination.SERVER;
    }
}
