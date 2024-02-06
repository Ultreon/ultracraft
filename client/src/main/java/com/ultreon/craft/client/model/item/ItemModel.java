package com.ultreon.craft.client.model.item;

import com.badlogic.gdx.graphics.g3d.Model;
import com.ultreon.craft.client.resources.LoadableResource;

public interface ItemModel extends LoadableResource {
    Model getModel();
}
