package com.ultreon.craft.client.player;

import com.ultreon.craft.network.packets.AddPermissionPacket;
import com.ultreon.craft.network.packets.InitialPermissionsPacket;
import com.ultreon.craft.network.packets.RemovePermissionPacket;
import com.ultreon.craft.server.player.PermissionMap;

public class ClientPermissionMap extends PermissionMap {
    public void onPacket(AddPermissionPacket packet) {
        this.allows.add(packet.getPermission());
    }

    public void onPacket(RemovePermissionPacket packet) {
        this.allows.remove(packet.getPermission());
    }

    public void onPacket(InitialPermissionsPacket packet) {
        this.allows.addAll(packet.getPermissions());
    }
}
