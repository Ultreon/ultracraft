package com.ultreon.craft.client.world;

import com.badlogic.gdx.math.Vector3;
import com.ultreon.libs.translations.v1.Language;

import java.util.Locale;

public enum BlockFace {
    TOP(new Vector3(0, 1, 0)),
    BOTTOM(new Vector3(0, -1, 0)),
    LEFT(new Vector3(-1, 0, 0)),
    RIGHT(new Vector3(1, 0, 0)),
    FRONT(new Vector3(0, 0, 1)),
    BACK(new Vector3(0, 0, -1));

    private final Vector3 normal;

    BlockFace(Vector3 normal) {
        this.normal = normal;
    }

    public Vector3 getNormal() {
        return this.normal;
    }

    public void getDisplayName() {
        Language.translate("craft.misc.blockFace." + this.name().toLowerCase(Locale.ROOT));
    }
}
