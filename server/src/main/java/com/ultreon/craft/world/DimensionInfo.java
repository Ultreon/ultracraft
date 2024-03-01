package com.ultreon.craft.world;

import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class DimensionInfo {
    @ApiStatus.Experimental // Do not use: SPOILER ALERT!
    public static final DimensionInfo SPACE = new DimensionInfo(new Identifier("space"));

    public static final DimensionInfo OVERWORLD = new DimensionInfo(new Identifier("overworld"));

    @ApiStatus.Experimental // Do not use: SPOILER ALERT!
    public static final DimensionInfo UNDERWORLD = new DimensionInfo(new Identifier("underworld"));

    @ApiStatus.Experimental // Do not use: SPOILER ALERT!
    public static final DimensionInfo ABYSS = new DimensionInfo(new Identifier("abyss"));

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
