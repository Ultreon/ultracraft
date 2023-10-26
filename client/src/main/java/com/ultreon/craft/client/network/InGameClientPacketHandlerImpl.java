package com.ultreon.craft.client.network;

import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.DisconnectedScreen;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InGameClientPacketHandlerImpl implements InGameClientPacketHandler {
    private final Connection connection;
    private final Map<Identifier, NetworkChannel> channels = new HashMap<>();
    private final PacketContext context;
    private final UltracraftClient client = UltracraftClient.get();
    private final Map<ChunkPos, ByteArrayOutputStream> chunkParts = new ConcurrentHashMap<>();

    public InGameClientPacketHandlerImpl(Connection connection) {
        this.connection = connection;
        this.context = new PacketContext(null, connection, EnvType.CLIENT);
    }

    public NetworkChannel registerChannel(Identifier id) {
        NetworkChannel networkChannel = NetworkChannel.create(id);
        this.channels.put(id, networkChannel);
        return networkChannel;
    }

    @Override
    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, EnvType.CLIENT));
    }

    @Override
    public NetworkChannel getChannel(Identifier channelId) {
        return this.channels.get(channelId);
    }

    @Override
    public void onPlayerHealth(float newHealth) {
        if (this.client.player != null) {
            this.client.player.onHealthUpdate(newHealth);
        }
    }

    @Override
    public void onRespawn(Vec3d pos) {
        ClientPlayer player = this.client.player;
        if (this.client.player != null) {
            player.setPosition(pos);
            player.resurrect();
        }
        this.client.showScreen(null);
    }

    @Override
    public void onPlayerSetPos(Vec3d pos) {
        ClientPlayer player = this.client.player;
        if (player != null) {
            player.setPosition(pos);
        }
    }

    @Override
    public void onChunkStart(ChunkPos pos, int dataLength) {
        ByteArrayOutputStream byteBuf = this.chunkParts.get(pos);
        if (byteBuf != null) {
            try {
                byteBuf.close();
            } catch (IOException ignored) {

            }
        }
        this.chunkParts.put(pos, new ByteArrayOutputStream());
    }

    @Override
    public void onChunkPart(ChunkPos pos, byte[] partialData) {
        this.chunkParts.get(pos).writeBytes(partialData);
    }

    @Override
    public void onChunkFinish(ChunkPos pos) {
        ByteArrayOutputStream remove = this.chunkParts.remove(pos);
        ClientWorld world = this.client.world;
        if (world != null) {
            world.loadChunk(pos, remove.toByteArray());
        }
        try {
            remove.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketDestination destination() {
        return null;
    }

    @Override
    public void onDisconnect(String message) {
        this.client.submit(() -> {
            this.client.renderWorld = false;
            @Nullable ClientWorld world = this.client.world;
            if (world != null) {
                world.dispose();
                this.client.world = null;
            }
            @Nullable WorldRenderer worldRenderer = this.client.worldRenderer;
            if (worldRenderer != null) {
                worldRenderer.dispose();
                this.client.worldRenderer = null;
            }

            IntegratedServer singleplayerServer = this.client.getSingleplayerServer();
            singleplayerServer.shutdown();
            this.client.showScreen(new DisconnectedScreen(message));
        });
    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return this.context;
    }
}
