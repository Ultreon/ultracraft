package com.ultreon.craft.network;

import com.ultreon.craft.network.packets.Packet;
import com.ultreon.libs.commons.v0.tuple.Pair;

public class PacketData<T extends PacketHandler> {
    private final PacketCollection<T> collection;

    public PacketData(PacketCollection<T> collection) {
        this.collection = collection;
    }

    public Packet<?> decode(int id, PacketBuffer buffer) {
        return this.collection.decode(id, buffer);
    }

    public void encode(Packet<?> packet, PacketBuffer buffer) {
        this.collection.encode(packet, buffer);
    }

    public void handle(Packet<T> packet, PacketContext context, T listener) {
        this.collection.handle(packet, new Pair<>(context, listener));
    }

    public int getId(Packet<?> msg) {
        return this.collection.getId(msg);
    }
}
