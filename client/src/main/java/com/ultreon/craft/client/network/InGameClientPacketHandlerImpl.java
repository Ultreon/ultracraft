package com.ultreon.craft.client.network;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.DisconnectedScreen;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.c2s.C2SChunkStatusPacket;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
            player.setVelocity(new Vec3d());
        }
    }

    @Override
    public void onChunkData(ChunkPos pos, short[] palette, List<Block> data) {
        ClientWorld world = this.client.world;

        PaletteStorage<Block> storage = new PaletteStorage<>(palette, data);

        if (world == null) {
            UltracraftClient.LOGGER.warn("World is not available when chunk load packet got received.");
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            return;
        }

        UltracraftClient.LOGGER.debug("Chunk %s finished".formatted(pos));
        world.loadChunk(pos, new ClientChunk(world, World.CHUNK_SIZE, World.CHUNK_HEIGHT, pos, storage));
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
        this.client.connection.close();

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

            this.connection.close();

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

    @Override
    public void onPlayerPositions(PacketContext ctx, List<Vec3d> list) {
        this.client.remotePlayers = list; //! Unoptimized system.
    }
}
