//package com.ultreon.craft.network.packets.c2s;
//
//import com.ultreon.craft.network.PacketBuffer;
//import com.ultreon.craft.network.PacketContext;
//import com.ultreon.craft.network.packets.Packet;
//import com.ultreon.craft.network.server.InGameServerPacketHandler;
//import com.ultreon.craft.world.container.ContainerInteraction;
//
//public class C2SContainerClickPacket extends Packet<InGameServerPacketHandler> {
//    private final int slot;
//    private final ContainerInteraction interaction;
//
//    public C2SContainerClickPacket(int slot, ContainerInteraction interaction) {
//        this.slot = slot;
//        this.interaction = interaction;
//    }
//
//    public C2SContainerClickPacket(PacketBuffer buffer) {
//        this.slot = buffer.readVarInt();
//        this.interaction = ContainerInteraction.values()[buffer.readByte()];
//    }
//
//    @Override
//    public void toBytes(PacketBuffer buffer) {
//        buffer.writeVarInt(slot);
//        buffer.writeByte(interaction.ordinal());
//    }
//
//    @Override
//    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
//        handler.handleContainerClick(slot, interaction);
//    }
//
//}
