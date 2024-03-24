package com.ultreon.craft.server.dedicated;

import java.io.FileWriter;

class ServerConfig {
    public String hostname = "localhost";
    public int port = 38800;
    public boolean allowFlying = false;
    public int maxPlayers = 15;

    public void serialize(FileWriter writer) {

    }
}
