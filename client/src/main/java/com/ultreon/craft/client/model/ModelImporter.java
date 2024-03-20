package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.g3d.Model;

public interface ModelImporter {
    Model getModel();

    Model createModel();
}
