package com.ultreon.craft.util;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.ultreon.craft.block.Block;

public class HitResult {
    // input
    public Ray ray;
    public float distanceMax = 5.0F;
    // output
    public Vector3 position = new Vector3();
    public Vector3 normal = new Vector3();
    public GridPoint3 pos = new GridPoint3();
    public GridPoint3 next = new GridPoint3();
    public Block block;
    public boolean collide;
    public float distance;

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