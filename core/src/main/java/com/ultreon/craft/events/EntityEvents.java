package com.ultreon.craft.events;

import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.EventResult;
import com.ultreon.libs.events.v1.ValueEventResult;

public class EntityEvents {
    public static final Event<Damage> DAMAGE = Event.withValue();
    public static final Event<Death> DEATH = Event.withResult();

    @FunctionalInterface
    public interface Damage {
        ValueEventResult<Float> onEntityDamage(LivingEntity entity, DamageSource source, float damage);
    }

    @FunctionalInterface
    public interface Death {
        EventResult onEntityDeath(LivingEntity entity);
    }
}
