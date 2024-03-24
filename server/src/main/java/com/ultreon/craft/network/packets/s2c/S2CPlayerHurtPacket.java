package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;

public class S2CPlayerHurtPacket extends Packet<InGameClientPacketHandler> {
    private final float damage;
    private final DamageSource source;

    public S2CPlayerHurtPacket(float damage, DamageSource source) {
        this.damage = damage;
        this.source = source;
    }

    public S2CPlayerHurtPacket(PacketBuffer buffer) {
        this.damage = buffer.readFloat();
        var source = Registries.DAMAGE_SOURCE.getElement(buffer.readId());
        if (source == null) {
            source = DamageSource.NOTHING;
        }
        this.source = source;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        Identifier type = this.source.getType();
        buffer.writeFloat(this.damage);
        buffer.writeId(type == null ? new Identifier("none") : type);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerHurt(this);
    }

    public float getDamage() {
        return this.damage;
    }

    public DamageSource getSource() {
        return this.source;
    }
}
