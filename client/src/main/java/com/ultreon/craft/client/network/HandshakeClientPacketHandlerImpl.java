package com.ultreon.craft.client.network;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.DisconnectedScreen;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.HandshakeClientPacketHandler;

public class HandshakeClientPacketHandlerImpl implements HandshakeClientPacketHandler {
    private final UltracraftClient client = UltracraftClient.get();
    private final Connection connection;

    public HandshakeClientPacketHandlerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onAcknowledge() {
        this.connection.setupCipher(this.client.getSecretKey());
        this.connection.moveToLogin();
        this.connection.setHandler(new LoginClientPacketHandlerImpl(this.connection));
    }

    @Override
    public void onDisconnect(String message) {
        this.connection.close();
        this.client.showScreen(new DisconnectedScreen(message));
    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return null;
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}
