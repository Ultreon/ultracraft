package com.ultreon.craft.client.network;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.rpc.Activity;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.LoginClientPacketHandler;

import java.util.UUID;

public class LoginClientPacketHandlerImpl implements LoginClientPacketHandler {
    private final UltracraftClient client = UltracraftClient.get();
    private final Connection connection;

    public LoginClientPacketHandlerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onLoginAccepted(UUID uuid) {
        ClientWorld clientWorld = new ClientWorld(this.client);
        this.client.world = clientWorld;
        this.client.player = new ClientPlayer(EntityTypes.PLAYER, clientWorld, uuid);
        this.client.connection.moveToInGame();
        this.client.connection.setHandler(new InGameClientPacketHandlerImpl(this.connection));

        this.client.submit(() -> {
            this.client.worldRenderer = new WorldRenderer(clientWorld);
            this.client.renderWorld = true;

            this.client.setActivity(Activity.SINGLEPLAYER);
        });
    }

    @Override
    public void onDisconnect(String message) {

    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return null;
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}
