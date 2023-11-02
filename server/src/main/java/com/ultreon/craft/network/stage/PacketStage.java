package com.ultreon.craft.network.stage;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketCollection;
import com.ultreon.craft.network.PacketData;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.S2CDisconnectPacket;
import com.ultreon.craft.network.packets.ingame.C2SKeepAlivePacket;
import com.ultreon.craft.network.packets.ingame.S2CKeepAlivePacket;
import com.ultreon.craft.network.server.ServerPacketHandler;

import java.util.function.Function;

public abstract class PacketStage {
    private final PacketCollection<ClientPacketHandler> clientBoundList = new PacketCollection<>();
    private final PacketCollection<ServerPacketHandler> serverBoundList = new PacketCollection<>();
    private final PacketData<ClientPacketHandler> clientData;
    private final PacketData<ServerPacketHandler> serverData;

    /**
     * Constructs a new packet stage.
     */
    @SuppressWarnings("unchecked")
    public PacketStage() {
        this.addServerBound(C2SDisconnectPacket::new);
        this.addServerBound(C2SKeepAlivePacket::new);
        this.addClientBound(S2CDisconnectPacket::new);
        this.addClientBound(S2CKeepAlivePacket::new);
        this.registerPackets();
        this.clientData = new PacketData<>(this.clientBoundList);
        this.serverData = new PacketData<>(this.serverBoundList);
    }

    /**
     * Registers all packets in this packet stage.
     */
    public abstract void registerPackets();

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param decoder the packet decoder
     * @param typeGetter the type getter for the packet
     * @param <T> the type of the packet
     * @return the id of the packet
     */
    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    protected <T extends Packet<? extends ServerPacketHandler>> int addServerBound(Function<PacketBuffer, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return this.serverBoundList.add(type, Packet::toBytes, t -> (Packet<ServerPacketHandler>) decoder.apply(t), (o, o2) -> o.handle(o2.getFirst(), o2.getSecond()));
    }

    /**
     * Adds a client-bound packet to this packet stage.
     *
     * @param decoder the packet decoder
     * @param typeGetter the type getter for the packet
     * @param <T> the type of the packet
     * @return the id of the packet
     */
    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    protected <T extends Packet<?>> int addClientBound(Function<PacketBuffer, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return this.clientBoundList.add(type, Packet::toBytes, t -> (Packet<ClientPacketHandler>) decoder.apply(t), (o, o2) -> o.handle(o2.getFirst(), o2.getSecond()));
    }

    /**
     * @return the client bound packet data.
     */
    public PacketData<ClientPacketHandler> getClientBoundData() {
        return this.clientData;
    }

    /**
     * @return the server bound packet data.
     */
    public PacketData<ServerPacketHandler> getServerData() {
        return this.serverData;
    }
}
