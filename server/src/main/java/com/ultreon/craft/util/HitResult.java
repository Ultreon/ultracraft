package com.ultreon.craft.util;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public class HitResult {
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    protected Vec3d position = new Vec3d();
    protected Vec3d normal = new Vec3d();
    protected Vec3i pos = new Vec3i();
    protected Vec3i next = new Vec3i();
    public Block block = Blocks.AIR;
    public boolean collide;
    public double distance;

    public HitResult() {

    }

    public HitResult(Ray ray) {
        this.ray = ray;
    }

    public HitResult(Ray ray, float distanceMax) {
        this.ray = ray;
        this.distanceMax = distanceMax;
    }

    public HitResult setInput(Ray ray){
        this.ray = ray;
        return this;
    }

    public Ray getRay() {
        return this.ray;
    }

    public float getDistanceMax() {
        return this.distanceMax;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public Vec3d getNormal() {
        return this.normal;
    }

    public Vec3i getPos() {
        return this.pos;
    }

    public Vec3i getNext() {
        return this.next;
    }

    public Block getBlock() {
        return this.block;
    }

    public boolean isCollide() {
        return this.collide;
    }

    public double getDistance() {
        return this.distance;
    }
}