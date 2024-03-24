package com.ultreon.craft.network.api;

import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record NetworkContext(PacketBuffer buffer, PacketDestination direction, Connection connection, @Nullable ServerPlayer sender) {
    public void enqueueWork(Runnable task) {

    }
}
