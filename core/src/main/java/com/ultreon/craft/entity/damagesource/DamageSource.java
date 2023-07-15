package com.ultreon.craft.entity.damagesource;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v1.Language;

public class DamageSource {
    public static final DamageSource FALLING = new DamageSource(UltreonCraft.id("falling"));
    public static final DamageSource VOID = new DamageSource(UltreonCraft.id("void"));

    private final Identifier type;

    public DamageSource(Identifier type) {
        this.type = type;
    }

    public Identifier getType() {
        return this.type;
    }

    public String getDescription() {
        return Language.translate("damageSource." + this.type.location() + "." + this.type.path().replaceAll("/", "."));
    }
}
