package com.ultreon.craft.entity.damagesource;

import com.google.common.base.Suppliers;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.Formatter;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DamageSource {
    public static final DamageSource NOTHING = DamageSource.register(new Identifier("none"), new DamageSource());
    public static final DamageSource FALLING = DamageSource.register(new Identifier("falling"), new DamageSource());
    public static final DamageSource VOID = DamageSource.register(new Identifier("void"), new DamageSource().byPassInvincibility(true));
    public static final DamageSource KILL = DamageSource.register(new Identifier("kill"), new DamageSource().byPassInvincibility(true));

    private final Supplier<TextObject> description = Suppliers.memoize(() -> {
        Identifier type = this.getType();
        if (type == null) return Formatter.format("<red>NULL</>");
        return TextObject.translation(type.namespace() + ".damageSource." + type.path().replaceAll("/", "."));
    });

    private boolean byPassInvincibility;

    private static <T extends DamageSource> T register(Identifier id, T damageSource) {
        Registries.DAMAGE_SOURCE.register(id, damageSource);
        return damageSource;
    }

    public @Nullable Identifier getType() {
        return Registries.DAMAGE_SOURCE.getId(this);
    }

    public TextObject getDescription() {
        return this.description.get();
    }

    public boolean byPassInvincibility() {
        return this.byPassInvincibility;
    }

    public @This DamageSource byPassInvincibility(boolean byPassInvincibility) {
        this.byPassInvincibility = byPassInvincibility;
        return this;
    }
}
