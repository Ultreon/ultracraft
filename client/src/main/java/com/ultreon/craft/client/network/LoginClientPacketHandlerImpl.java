package com.ultreon.craft.client.network;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.events.ClientPlayerEvents;
import com.ultreon.craft.client.player.LocalPlayer;
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
        this.client.connection.moveToInGame();
        this.client.connection.setHandler(new InGameClientPacketHandlerImpl(this.connection));

        ClientWorld clientWorld = new ClientWorld(this.client);
        this.client.world = clientWorld;
        var player = this.client.player = new LocalPlayer(EntityTypes.PLAYER, clientWorld, uuid);

        if (this.client.integratedServer != null) {
            this.client.integratedServer.loadPlayer(player);
        }

        System.out.println("Logged in with uuid: " + uuid);

        ClientPlayerEvents.PLAYER_JOINED.factory().onPlayerJoined(player);

        this.client.submit(() -> {
            try {
                this.client.worldRenderer = new WorldRenderer(clientWorld);
                this.client.renderWorld = true;
                this.client.showScreen(null);
            } catch (Exception e) {
                UltracraftClient.crash(e);
            }

            if (this.client.integratedServer != null) this.client.setActivity(Activity.SINGLEPLAYER);
            else this.client.setActivity(Activity.MULTIPLAYER);
        });
    }

    @Override
    public void onDisconnect(String message) {
        this.connection.close();
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
