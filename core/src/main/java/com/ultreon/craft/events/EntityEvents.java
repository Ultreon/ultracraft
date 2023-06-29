package com.ultreon.craft.events;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.ValueEventResult;

public class EntityEvents {
    public static final Event<Damage> DAMAGE = Event.withValue();

    @FunctionalInterface
    public interface Damage {
        ValueEventResult<Float> onEntityDamage(LivingEntity entity, DamageSource source, float damage);
    }
}
