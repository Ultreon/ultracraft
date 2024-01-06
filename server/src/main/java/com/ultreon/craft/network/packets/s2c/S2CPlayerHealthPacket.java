package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CPlayerHealthPacket extends Packet<InGameClientPacketHandler> {
    private final float newHealth;

    public S2CPlayerHealthPacket(float newHealth) {
        this.newHealth = newHealth;
    }

    public S2CPlayerHealthPacket(PacketBuffer buffer) {
        this.newHealth = buffer.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeFloat(this.newHealth);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerHealth(this.newHealth);
    }
}
