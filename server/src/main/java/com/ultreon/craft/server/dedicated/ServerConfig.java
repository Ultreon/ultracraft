package com.ultreon.craft.server.dedicated;

import com.ultreon.craft.config.crafty.ConfigEntry;
import com.ultreon.craft.config.crafty.ConfigInfo;
import com.ultreon.craft.config.crafty.CraftyConfig;
import com.ultreon.craft.config.crafty.RequiresRestart;

@ConfigInfo(fileName = "ultreon-server")
public class ServerConfig extends CraftyConfig {
    @ConfigEntry(path = "hosting.hostname", comment = "The hostname to use for the server.")
    @RequiresRestart
    public static String hostname = "localhost";

    @ConfigEntry(path = "hosting.port", comment = "The port to use for the server.")
    @RequiresRestart
    public static int port = 38800;

    @ConfigEntry(path = "antiCheat.allowFlying", comment = "Whether to allow unauthorized players to fly.")
    public static boolean allowFlying = false;

    @ConfigEntry(path = "security.maxPlayers", comment = "The maximum number of players allowed on the server.")
    public static int maxPlayers = 10;
}
