package com.ultreon.craft.server.dedicated;

import java.security.SecureRandom;

class ServerConfig {
    public String hostname = "localhost";
    public int port = 38800;
    public String password = this.generateRandomPassword();

    public boolean allowFlying = false;

    public int maxPlayers = 15;

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        random.setSeed(System.currentTimeMillis() ^ random.nextLong());

        var symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.!_+";
        StringBuilder password = new StringBuilder();
        for (var i = 0; i < 10; i++) {
            password.append(symbols.charAt(random.nextInt(symbols.length())));
        }
        return password.toString();
    }
}
