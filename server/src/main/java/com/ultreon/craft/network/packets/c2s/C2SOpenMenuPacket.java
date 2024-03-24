package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import org.jetbrains.annotations.Nullable;

public class C2SOpenMenuPacket extends Packet<InGameServerPacketHandler> {
    private final Identifier id;
    private final BlockPos pos;

    public C2SOpenMenuPacket(PacketBuffer buffer) {
        this.id = buffer.readId();
        this.pos = buffer.readBoolean() ? buffer.readBlockPos() : null;
    }

    public C2SOpenMenuPacket(Identifier id, @Nullable BlockPos pos) {
        this.id = id;
        this.pos = pos;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeId(id);
        buffer.writeBoolean(pos != null);
        if (pos != null) {
            buffer.writeBlockPos(pos);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.handleOpenMenu(id, pos);
    }
}
