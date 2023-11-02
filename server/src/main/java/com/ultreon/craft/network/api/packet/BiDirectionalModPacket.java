package com.ultreon.craft.network.api.packet;

import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.server.player.ServerPlayer;

import java.util.function.Supplier;

public abstract non-sealed class BiDirectionalModPacket<T extends BiDirectionalModPacket<T>> extends ModPacket<T> implements ClientEndpoint, ServerEndpoint {
    public BiDirectionalModPacket() {
        super();
    }

    @Override
    public final boolean handle(Supplier<ModPacketContext> context) {
        PacketContext ctx = context.get();
        switch (ctx.getDestination()) {
            case CLIENT -> ctx.queue(this::handleClient);
            case SERVER -> ctx.queue(() -> this.handleServer(ctx.getPlayer()));
            default -> {
            }
        }
        return true;
    }

    protected abstract void handleClient();

    protected abstract void handleServer(ServerPlayer sender);
}
