package com.ultreon.craft.util;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.ultreon.craft.block.Block;

public class HitResult {
    // input
    protected Ray ray;
    protected float distanceMax = 5.0F;
    // output
    protected Vector3 position = new Vector3();
    protected Vector3 normal = new Vector3();
    protected GridPoint3 pos = new GridPoint3();
    protected GridPoint3 next = new GridPoint3();
    protected Block block;
    protected boolean collide;
    protected float distance;

    public HitResult() {

    }

    public HitResult(Ray ray) {
        this.ray = ray;
    }

    public HitResult(Ray ray, float distanceMax) {
        this.ray = ray;
    }

    public HitResult setInput(Ray ray){
        this.ray = ray;
        return this;
    }

    public Ray getRay() {
        return ray;
    }

    public float getDistanceMax() {
        return distanceMax;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getNormal() {
        return normal;
    }

    public GridPoint3 getPos() {
        return pos;
    }

    public GridPoint3 getNext() {
        return next;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isCollide() {
        return collide;
    }

    public float getDistance() {
        return distance;
    }
}