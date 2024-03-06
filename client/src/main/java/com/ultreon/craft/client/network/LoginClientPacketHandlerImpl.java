package com.ultreon.craft.client.network;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.api.events.ClientPlayerEvents;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.rpc.GameActivity;
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
    private boolean disconnected;

    public LoginClientPacketHandlerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onLoginAccepted(UUID uuid) {
        this.client.connection.moveToInGame();
        this.client.connection.setHandler(new InGameClientPacketHandlerImpl(this.connection));

        ClientWorld clientWorld = new ClientWorld(this.client);
        this.client.world = clientWorld;
        this.client.inspection.createNode("world", () -> this.client.world);

        var player = this.client.player = new LocalPlayer(EntityTypes.PLAYER, clientWorld, uuid);
        clientWorld.spawn(player);

        Connection.LOGGER.info("Logged in with uuid: {}", uuid);

        ClientPlayerEvents.PLAYER_JOINED.factory().onPlayerJoined(player);

        if (this.client.integratedServer != null) this.client.setActivity(GameActivity.SINGLEPLAYER);
        else this.client.setActivity(GameActivity.MULTIPLAYER);

        UltracraftClient.invoke(() -> {
            this.client.worldRenderer = new WorldRenderer(this.client.world);
            this.client.renderWorld = true;
            this.client.showScreen(null);
        });
    }

    @Override
    public void onDisconnect(String message) {
        this.disconnected = true;
        this.connection.closeAll();
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

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
