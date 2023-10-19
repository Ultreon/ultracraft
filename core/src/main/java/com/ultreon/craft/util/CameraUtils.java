package com.ultreon.craft.util;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class CameraUtils {
    private static final Vector3 tmpVec = new Vector3();

    public static void rotateAround(Matrix4 mat, Vector3 point, Vector3 axis, float angle) {
        tmpVec.set(point);
//        tmpVec.sub(position);
        mat.translate(tmpVec);
        mat.rotate(axis, angle);
        tmpVec.rotate(axis, angle);
        mat.translate(-tmpVec.x, -tmpVec.y, -tmpVec.z);
    }

}
