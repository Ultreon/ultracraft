package com.ultreon.craft.network.packets;

import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;

public class AddPermissionPacket extends Packet<InGameClientPacketHandler> {
    private final Permission permission;

    public AddPermissionPacket(Permission permission) {
        this.permission = permission;
    }

    public AddPermissionPacket(PacketBuffer buffer) {
        this.permission = new Permission(buffer.readString(128));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.permission.toString(), 128);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onAddPermission(this);
    }

    public Permission getPermission() {
        return this.permission;
    }
}
