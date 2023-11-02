package com.ultreon.craft.network.client;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.List;
import java.util.UUID;

public interface InGameClientPacketHandler extends ClientPacketHandler {
    void onModPacket(NetworkChannel channel, ModPacket<?> packet);

    NetworkChannel getChannel(Identifier channelId);

    void onPlayerHealth(float newHealth);

    void onRespawn(Vec3d pos);

    void onPlayerSetPos(Vec3d pos);

    void onChunkCancel(ChunkPos pos);

    void onChunkData(ChunkPos pos, short[] palette, List<Block> data);

    void onPlayerPosition(PacketContext ctx, UUID player, Vec3d pos);

    void onKeepAlive();

    void onPlaySound(Identifier sound, float volume);

    void onAddPlayer(UUID uuid, String name, Vec3d position);

    void onRemovePlayer(UUID u);
}
