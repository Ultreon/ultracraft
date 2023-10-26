package com.ultreon.craft.network.client;

import java.util.UUID;

public interface LoginClientPacketHandler extends ClientPacketHandler {

    void onLoginAccepted(UUID uuid);
}
