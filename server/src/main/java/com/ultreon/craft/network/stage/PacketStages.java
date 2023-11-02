package com.ultreon.craft.network.stage;

public class PacketStages {
    public static final HandshakePacketStage HANDSHAKE = new HandshakePacketStage();
    public static final LoginPacketStage LOGIN = new LoginPacketStage();
    public static final InGamePacketStage IN_GAME = new InGamePacketStage();
}
