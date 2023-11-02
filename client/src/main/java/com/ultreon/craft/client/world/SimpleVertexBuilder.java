package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.checkerframework.common.returnsreceiver.qual.This;

public class SimpleVertexBuilder extends VertexBuilder {
    public SimpleVertexBuilder() {

    }

    public Vertex<SimpleVertexBuilder> vertex(float x, float y, float z) {
        return new Vertex<>(this, x, y, z);
    }

    public OffsetBuilder offset(float offX, float offY, float offZ, int normalX, int normalY, int notmalZ, TextureRegion tex) {
        return new OffsetBuilder(this, offX, offY, offZ, normalX, normalY, notmalZ, tex);
    }

    public float[] end() {
        float[] items = this.vertices.items;
        this.vertices.clear();
        this.vertices.items = null;
        this.vertices = null;
        return items;
    }

    public static final class Vertex<B extends VertexBuilder> {
        private final B builder;
        Vector3 position = new Vector3();
        Vector3 normal = new Vector3();
        Vector2 uv = new Vector2();

        public Vertex(B builder, float x, float y, float z) {
            this.builder = builder;
            this.position.set(x, y, z);
        }

        public @This Vertex<B> normal(float x, float y, float z) {
            this.normal.set(x, y, z);
            return this;
        }

        public @This Vertex<B> uv(float u, float v) {
            this.uv.set(u, v);
            return this;
        }

        public B end() {
            this.builder.vertices.addAll(
                    this.position.x, this.position.y, this.position.z,
                    this.normal.x, this.normal.y, this.normal.z,
                    this.uv.x, this.uv.y
            );
            return this.builder;
        }
    }

    public static class OffsetBuilder extends VertexBuilder {
        private final SimpleVertexBuilder parent;
        private final TextureRegion tex;
        private final Vector3 offset = new Vector3();
        private final Vector3 normal = new Vector3();

        public OffsetBuilder(SimpleVertexBuilder vertexBuilder, float x, float y, float z, int nx, int ny, int nz, TextureRegion tex) {
            this.parent = vertexBuilder;
            this.offset.set(x, y, z);
            this.normal.set(nx, ny, nz);
            this.tex = tex;
        }

        public Vertex<OffsetBuilder> vertex(float x, float y, float z) {
            Vertex<OffsetBuilder> vertex = new Vertex<>(this, this.offset.x + x, this.offset.x + y, this.offset.y + z);
            vertex.normal.set(this.normal);
            return vertex;
        }

        public SimpleVertexBuilder end() {
            this.parent.vertices.addAll(this.vertices);
            this.vertices.clear();
            this.vertices.items = null;
            this.vertices = null;
            return this.parent;
        }
    }
}
