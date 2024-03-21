package com.ultreon.craft.client.model.item;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.resources.LoadableResource;

public interface ItemModel extends LoadableResource {

    Vector3 DEFAULT_SCALE = new Vector3(1, 1, 1);

    Model getModel();

    default Vector3 getScale() {
        return DEFAULT_SCALE;
    }

    default Vector3 getOffset() {
        return Vector3.Zero;
    }
}
