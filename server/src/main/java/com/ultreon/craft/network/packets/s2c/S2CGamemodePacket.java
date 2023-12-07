package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.util.Gamemode;

public class S2CGamemodePacket extends Packet<InGameClientPacketHandler> {
    private final Gamemode gamemode;

    public S2CGamemodePacket(Gamemode gamemode) {
        this.gamemode = gamemode;
    }

    public S2CGamemodePacket(PacketBuffer buffer) {
        this.gamemode = Gamemode.byOrdinal(buffer.readByte());
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(this.gamemode.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onGamemode(this.gamemode);
    }
}
