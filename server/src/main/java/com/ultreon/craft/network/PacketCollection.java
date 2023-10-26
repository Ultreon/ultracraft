package com.ultreon.craft.network;

import com.google.common.base.Preconditions;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.libs.commons.v0.tuple.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketCollection<H extends PacketHandler> {
    private int id;
    private final Map<Class<? extends Packet<?>>, BiConsumer<Packet<?>, PacketBuffer>> encoders = new HashMap<>();
    private final Int2ObjectMap<Function<PacketBuffer, ? extends Packet<H>>> decoders = new Int2ObjectArrayMap<>();
    private final Map<Class<? extends Packet<?>>, BiConsumer<Packet<H>, Pair<PacketContext, H>>> handlers = new HashMap<>();
    private final Map<Class<? extends Packet<?>>, Integer> packet2id = new HashMap<>();

    public int add(Class<? extends Packet<?>> type, BiConsumer<Packet<?>, PacketBuffer> encoder, Function<PacketBuffer, Packet<H>> decoder, BiConsumer<Packet<H>, Pair<PacketContext, H>> handler) {
        this.encoders.put(type, encoder);
        this.decoders.put(this.id, decoder);
        this.handlers.put(type, handler);
        this.packet2id.put(type, this.id);
        return this.id++;
    }

    public void encode(Packet<?> packet, PacketBuffer buffer) {
        Preconditions.checkNotNull(packet, "packet");
        Preconditions.checkNotNull(buffer, "buffer");

        @Nullable BiConsumer<Packet<?>, PacketBuffer> encoder = this.encoders.get(packet.getClass());
        if (encoder == null) throw new PacketException("Unknown packet: " + packet.getClass());
        encoder.accept(packet, buffer);
    }

    public Packet<H> decode(int id, PacketBuffer buffer) {
        Preconditions.checkNotNull(buffer, "buffer");

        Function<PacketBuffer, ? extends Packet<H>> decoder = this.decoders.get(id);
        if (decoder == null) throw new PacketException("Unknown packet ID: " + id);
        return decoder.apply(buffer);
    }

    public void handle(Packet<H> packet, Pair<PacketContext, H> params) {
        Preconditions.checkNotNull(packet, "packet");
        Preconditions.checkNotNull(params, "params");

        BiConsumer<Packet<H>, Pair<PacketContext, H>> handler = this.handlers.get(packet.getClass());
        if (handler == null) throw new PacketException("Unknown packet: " + packet.getClass());
        handler.accept(packet, params);
    }

    public int getId(Packet<?> packet) {
        Preconditions.checkNotNull(packet, "packet");

        Integer id = this.packet2id.get(packet.getClass());
        if (id == null) throw new PacketException("Unknown packet: " + packet.getClass());
        return id;
    }
}
