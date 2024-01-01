package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v1.Language;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class DimensionInfo {
    private final Identifier id;

    public DimensionInfo(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DimensionInfo that = (DimensionInfo) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String getName() {
        return Language.translate(this.id.location() + ".dimension." + this.id.path().replace('/', '.'));
    }
}
