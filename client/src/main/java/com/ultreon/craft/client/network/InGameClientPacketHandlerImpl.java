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
import com.ultreon.craft.network.packets.C2SChunkStatusPacket;
import com.ultreon.craft.util.Hashing;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Logger;
import com.ultreon.libs.commons.v0.vector.Vec3d;
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
    private final Map<ChunkPos, byte[]> chunkHashes = new ConcurrentHashMap<>();

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
    public void onChunkStart(ChunkPos pos, byte[] hash, int dataLength) {
        if (this.chunkParts.containsKey(pos)) {
            UltracraftClient.LOGGER.warn("Chunk starting while already started.");
            return;
        }

        ByteArrayOutputStream stream = this.chunkParts.get(pos);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                UltracraftClient.LOGGER.error("Can't close previously opened stream:", e);
            }
        }
        this.chunkHashes.put(pos, hash);
        this.chunkParts.put(pos, new ByteArrayOutputStream());
    }

    @Override
    public void onChunkPart(ChunkPos pos, byte[] partialData) {
        ByteArrayOutputStream bos = this.chunkParts.get(pos);
        if (!this.chunkParts.containsKey(pos)) {
            UltracraftClient.LOGGER.warn("[PART-CHECK] Chunk not started yet: " + pos);
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            this.chunkHashes.remove(pos);
            return;
        }
        if (!this.chunkHashes.containsKey(pos)) {
            UltracraftClient.LOGGER.warn("[HASH-CHECK] Chunk not started yet: " + pos);
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            try {
                this.chunkParts.remove(pos).close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        bos.writeBytes(partialData);
    }

    @Override
    public void onChunkFinish(ChunkPos pos) {
        if (!this.chunkParts.containsKey(pos)) {
            UltracraftClient.LOGGER.debug("Chunk %s has missing chunk parts".formatted(pos));

            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            this.chunkHashes.remove(pos);
            return;
        }
        if (!this.chunkHashes.containsKey(pos)) {
            UltracraftClient.LOGGER.debug("Chunk %s has missing chunk hashes".formatted(pos));

            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            this.chunkParts.remove(pos);
            return;
        }

        ByteArrayOutputStream stream = this.chunkParts.remove(pos);
        ClientWorld world = this.client.world;
        byte[] data = stream.toByteArray();
        byte[] remove = this.chunkHashes.remove(pos);
        if (!Hashing.verifyMD5(data, remove)) {
            UltracraftClient.LOGGER.warn("""
            Chunk hash don't match! World corruptions may occur.
              Original Hash: %s
              Current Hash: %s
              Chunk position: %s""".formatted(
                      InGameClientPacketHandlerImpl.byteArrayToHexString(remove), InGameClientPacketHandlerImpl.byteArrayToHexString(Hashing.hashMD5(data)), pos)
            );
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            return;
        }

        if (world != null) {
            UltracraftClient.LOGGER.debug("Chunk %s finished".formatted(pos));

            world.loadChunk(pos, data);
        } else {
            UltracraftClient.LOGGER.warn("World is not available when chunk load packet got received.");
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
        }
        try {
            stream.close();
        } catch (IOException e) {
            UltracraftClient.LOGGER.warn("Failed to close chunk i/o stream.");
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
        }
    }

    @Override
    public void onChunkCancel(ChunkPos pos) {
        try {
            this.chunkParts.remove(pos).close();
        } catch (IOException ignored) {

        }

        this.chunkHashes.remove(pos);

        this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder(2 * byteArray.length);
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
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
