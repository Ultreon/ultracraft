package com.ultreon.craft.entity;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.data.types.MapType;

public class S2CEntityPipeline extends Packet<InGameClientPacketHandler> {
    private int id;
    private final MapType pipeline;

    public S2CEntityPipeline(int id, MapType pipeline) {
        this.id = id;
        this.pipeline = pipeline;
    }

    public S2CEntityPipeline(PacketBuffer buffer) {
        this.id = buffer.readVarInt();
        pipeline = buffer.readUbo();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(id);
        buffer.writeUbo(pipeline);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onEntityPipeline(this.id, this.pipeline);
    }
}
