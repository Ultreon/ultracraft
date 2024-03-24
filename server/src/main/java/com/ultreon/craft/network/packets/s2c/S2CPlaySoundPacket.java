package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.util.Identifier;

public class S2CPlaySoundPacket extends Packet<InGameClientPacketHandler> {
    private final Identifier sound;
    private final float volume;

    public S2CPlaySoundPacket(Identifier sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    public S2CPlaySoundPacket(PacketBuffer buffer) {
        this.sound = buffer.readId();
        this.volume = buffer.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeId(this.sound);
        buffer.writeFloat(this.volume);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlaySound(this.sound, this.volume);
    }
}
