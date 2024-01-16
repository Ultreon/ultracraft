package com.ultreon.craft.network.api.packet;

import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.Env;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public abstract non-sealed class ModPacketToServer<T extends ModPacketToServer<T>> extends ModPacket<T> implements ServerEndpoint {
    @Override
    public final boolean handle(Supplier<ModPacketContext> context) {
        PacketContext ctx = context.get();
        if (ctx.getDestination() == Env.SERVER)
            ctx.queue(() -> this.handle(Objects.requireNonNull(ctx.getPlayer(), "Server player was not found while on server side")));
        return true;
    }

    protected abstract void handle(@NotNull ServerPlayer sender);
}
