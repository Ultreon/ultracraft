package com.ultreon.craft.network;

import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ClientEndpoint;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.packets.c2s.C2SModPacket;
import com.ultreon.craft.network.packets.s2c.S2CModPacket;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.libs.commons.v0.Identifier;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkChannel {
    private static final Map<Identifier, NetworkChannel> CHANNELS = new HashMap<>();
    private final Identifier key;
    private int curId;
    private final Reference2IntMap<Class<? extends ModPacket<?>>> idMap = new Reference2IntArrayMap<>();
    private final Map<Class<? extends ModPacket<?>>, BiConsumer<? extends ModPacket<?>, PacketBuffer>> encoders = new HashMap<>();
    private final Int2ReferenceMap<Function<PacketBuffer, ? extends ModPacket<?>>> decoders = new Int2ReferenceArrayMap<>();
    private final Map<Class<? extends ModPacket<?>>, BiConsumer<? extends ModPacket<?>, Supplier<ModPacketContext>>> consumers = new HashMap<>();

    @ClientOnly
    private Connection c2sConnection;

    private NetworkChannel(Identifier key) {
        this.key = key;
    }

    public static NetworkChannel create(Identifier id) {
        NetworkChannel channel = new NetworkChannel(id);
        NetworkChannel.CHANNELS.put(id, channel);
        return channel;
    }

    @CheckReturnValue
    public static NetworkChannel getChannel(Identifier channelId) {
        return NetworkChannel.CHANNELS.get(channelId);
    }

    @ClientOnly
    public void setC2sConnection(Connection connection) {
        this.c2sConnection = connection;
    }

    public Identifier id() {
        return this.key;
    }

    public <T extends ModPacket<T>> void register(Class<T> clazz, BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer, T> decoder, BiConsumer<T, Supplier<ModPacketContext>> packetConsumer) {
        this.curId++;
        this.idMap.put(clazz, this.curId);
        this.encoders.put(clazz, encoder);
        this.decoders.put(this.curId, decoder);
        this.consumers.put(clazz, packetConsumer);
    }

    public void queue(Runnable task) {

    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToPlayer(ServerPlayer player, ModPacket<T> modPacket) {
        player.connection.send(new S2CModPacket(this, modPacket));
    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToPlayers(List<ServerPlayer> players, ModPacket<T> modPacket) {
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            player.connection.send(new S2CModPacket(this, modPacket), false);
            if (i == players.size() - 1) {
                player.connection.send(new S2CModPacket(this, modPacket), true);
            }
        }
    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToServer(ModPacket<T> modPacket) {
        this.c2sConnection.send(new C2SModPacket(this, modPacket));
    }

    public Function<PacketBuffer, ? extends ModPacket<?>> getDecoder(int id) {
        return this.decoders.get(id);
    }

    public BiConsumer<? extends ModPacket<?>, PacketBuffer> getEncoder(Class<? extends ModPacket<?>> type) {
        return this.encoders.get(type);
    }

    public BiConsumer<? extends ModPacket<?>, Supplier<ModPacketContext>> getConsumer(Class<? extends ModPacket<?>> type) {
        return this.consumers.get(type);
    }

    public int getId(ModPacket<?> packet) {
        return this.idMap.getInt(packet.getClass());
    }
}
