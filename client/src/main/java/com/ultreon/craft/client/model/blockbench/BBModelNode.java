package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.math.Matrix4;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.UUID;

public sealed interface BBModelNode permits BBModelElement, BBModelGroup {
    Matrix4 rotationMatrix();

    UUID uuid();

    Vec3f rotation();

    BBModelNode parent();
}
