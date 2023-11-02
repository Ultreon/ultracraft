package com.ultreon.craft.network.api.packet;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.PacketBuffer;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public abstract sealed class ModPacket<T extends ModPacket<T>> permits BiDirectionalModPacket, ModPacketToClient, ModPacketToServer {
    protected abstract boolean handle(Supplier<ModPacketContext> context);

    @CanIgnoreReturnValue
    public final boolean handlePacket(Supplier<ModPacketContext> context) {
        try {
            this.handle(context);
        } catch (Throwable throwable) {
            Connection.LOGGER.error("Couldn't handle packet:", throwable);
        }
        return true;
    }

    public abstract void toBytes(PacketBuffer buffer);
}
