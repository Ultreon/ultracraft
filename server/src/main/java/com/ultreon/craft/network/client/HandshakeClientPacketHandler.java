package com.ultreon.craft.network.client;

public interface HandshakeClientPacketHandler extends ClientPacketHandler {
    void onAcknowledge();
}
