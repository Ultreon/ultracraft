package com.ultreon.craft.network.packets.handshake;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.HandshakeServerPacketHandler;
import com.ultreon.craft.network.server.LoginServerPacketHandler;

public class C2SHelloPacket extends Packet<HandshakeServerPacketHandler> {
    private final byte[] encryptedKey;

    public C2SHelloPacket(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public C2SHelloPacket(PacketBuffer buffer) {
        byte[] key = new byte[16];
        buffer.readBytes(key);
        this.encryptedKey = key;
    }

    public byte[] getEncryptedKey() {
        return this.encryptedKey;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        // Sent hash.
        buffer.writeBytes(this.encryptedKey);
    }

    @Override
    public void handle(PacketContext ctx, HandshakeServerPacketHandler handler) {
        handler.onHello(this.encryptedKey);
    }
}
