package com.ultreon.craft.network.stage;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketCollection;
import com.ultreon.craft.network.PacketData;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;

import java.util.function.Function;

public abstract class PacketStage {
    private final PacketCollection<ClientPacketHandler> clientBoundList = new PacketCollection<>();
    private final PacketCollection<ServerPacketHandler> serverBoundList = new PacketCollection<>();
    private final PacketData<ClientPacketHandler> clientData;
    private final PacketData<ServerPacketHandler> serverData;

    public PacketStage() {
        this.registerPackets();
        this.clientData = new PacketData<>(this.clientBoundList);
        this.serverData = new PacketData<>(this.serverBoundList);
    }

    public abstract void registerPackets();

    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    protected <T extends Packet<? extends ServerPacketHandler>> int addServerBound(Function<PacketBuffer, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return this.serverBoundList.add(type, Packet::toBytes, t -> (Packet<ServerPacketHandler>) decoder.apply(t), (o, o2) -> o.handle(o2.getFirst(), o2.getSecond()));
    }

    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    protected <T extends Packet<?>> int addClientBound(Function<PacketBuffer, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return this.clientBoundList.add(type, Packet::toBytes, t -> (Packet<ClientPacketHandler>) decoder.apply(t), (o, o2) -> o.handle(o2.getFirst(), o2.getSecond()));
    }

    public PacketData<ClientPacketHandler> getClientBoundData() {
        return this.clientData;
    }

    public PacketData<ServerPacketHandler> getServerData() {
        return this.serverData;
    }
}
