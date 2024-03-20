package com.ultreon.craft.world;

import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class DimensionInfo {
    public static final DimensionInfo OVERWORLD = new DimensionInfo(new Identifier("overworld"));
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

    public TextObject getName() {
        return TextObject.translation(this.id.namespace() + ".dimension." + this.id.path().replace('/', '.'));
    }
}
