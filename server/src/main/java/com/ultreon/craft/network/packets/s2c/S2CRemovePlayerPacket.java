package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class S2CRemovePlayerPacket extends Packet<InGameClientPacketHandler> {
    private final UUID uuid;

    public S2CRemovePlayerPacket(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public S2CRemovePlayerPacket(PacketBuffer buffer) {
        this.uuid = buffer.readUuid();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUuid(this.uuid);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemovePlayer(this.uuid);
    }
}
