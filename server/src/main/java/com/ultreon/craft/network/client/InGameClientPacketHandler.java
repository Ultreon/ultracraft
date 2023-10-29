package com.ultreon.craft.network.client;

import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public interface InGameClientPacketHandler extends ClientPacketHandler {
    void onModPacket(NetworkChannel channel, ModPacket<?> packet);

    NetworkChannel getChannel(Identifier channelId);

    void onPlayerHealth(float newHealth);

    void onRespawn(Vec3d pos);

    void onPlayerSetPos(Vec3d pos);

    void onChunkStart(ChunkPos pos, byte[] hash, int dataLength);

    void onChunkPart(ChunkPos pos, byte[] partialData);

    void onChunkFinish(ChunkPos pos);

    void onChunkCancel(ChunkPos pos);
}
