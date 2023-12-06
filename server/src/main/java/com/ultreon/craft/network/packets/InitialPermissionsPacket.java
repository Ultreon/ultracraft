package com.ultreon.craft.network.packets;

import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;

import java.util.List;

public class InitialPermissionsPacket extends Packet<InGameClientPacketHandler> {
    private final List<Permission> permissions;

    public InitialPermissionsPacket(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public InitialPermissionsPacket(PacketBuffer buffer) {
        this.permissions = buffer.readList((buf) -> new Permission(buffer.readUTF(128)));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeList(this.permissions, (buf, permission) -> buffer.writeUTF(this.permissions.toString(), 128));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onInitialPermissions(this);
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }
}
