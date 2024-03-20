package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class S2CTabCompletePacket extends Packet<InGameClientPacketHandler> {
    private final List<String> options;

    public S2CTabCompletePacket(@Nullable List<String> options) {
        this.options = options;
    }

    public S2CTabCompletePacket(PacketBuffer buffer) {
        this.options = buffer.readList(buf -> buf.readString(64));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeList(this.options, (buf, elem) -> buf.writeUTF(elem, 64));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onTabCompleteResult(this.options.toArray(new String[0]));
    }
}
