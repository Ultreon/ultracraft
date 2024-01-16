package com.ultreon.craft.network.api.packet;

import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.util.Env;

import java.util.function.Supplier;

public abstract non-sealed class ModPacketToClient<T extends ModPacketToClient<T>> extends ModPacket<T> implements ClientEndpoint {
    public ModPacketToClient() {
        super();
    }

    @Override
    public final boolean handle(Supplier<ModPacketContext> context) {
        PacketContext ctx = context.get();
        if (ctx.getDestination() == Env.CLIENT)
            ctx.queue(this::handle);
        return true;
    }

    protected abstract void handle();
}
