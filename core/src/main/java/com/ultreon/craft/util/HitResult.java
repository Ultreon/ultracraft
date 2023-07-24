package com.ultreon.craft.util;

import com.ultreon.craft.block.Block;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public class HitResult {
    // input
    public Ray ray;
    public float distanceMax = 5.0F;
    // output
    public Vec3d position = new Vec3d();
    public Vec3d normal = new Vec3d();
    public Vec3i pos = new Vec3i();
    public Vec3i next = new Vec3i();
    public Block block;
    public boolean collide;
    public double distance;

    public HitResult() {

    }

    public HitResult(Ray ray) {
        this.ray = ray;
    }

    public HitResult setInput(Ray ray){
        this.ray = ray;
        return this;
    }
}