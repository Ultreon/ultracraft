package com.ultreon.craft.network.packets.login;

import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.client.LoginClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

import java.util.UUID;

public class S2CLoginAcceptedPacket extends Packet<LoginClientPacketHandler> {
    private final UUID uuid;

    public S2CLoginAcceptedPacket(UUID uuid) {
        this.uuid = uuid;
    }

    public S2CLoginAcceptedPacket(PacketBuffer buffer) {
        this.uuid = buffer.readUuid();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUuid(this.uuid);
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onLoginAccepted(this.uuid);
    }
}
