package com.ultreon.craft.client.model;

import com.ultreon.craft.client.world.FaceProperties;

import java.util.Objects;

public class ModelProperties {
    public final FaceProperties top;
    public final FaceProperties bottom;
    public final FaceProperties left;
    public final FaceProperties right;
    public final FaceProperties front;
    public final FaceProperties back;

    public ModelProperties(FaceProperties top, FaceProperties bottom, FaceProperties left, FaceProperties right, FaceProperties front, FaceProperties back) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
    }

    public ModelProperties(FaceProperties all) {
        this(all, all, all, all, all, all);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ModelProperties that = (ModelProperties) o;
        return Objects.equals(this.top, that.top) && Objects.equals(this.bottom, that.bottom) && Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right) && Objects.equals(this.front, that.front) && Objects.equals(this.back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.top, this.bottom, this.left, this.right, this.front, this.back);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FaceProperties top = new FaceProperties();
        private FaceProperties bottom = new FaceProperties();
        private FaceProperties left = new FaceProperties();
        private FaceProperties right = new FaceProperties();
        private FaceProperties front = new FaceProperties();
        private FaceProperties back = new FaceProperties();

        public Builder top(FaceProperties top) {
            this.top = top;
            return this;
        }

        public Builder bottom(FaceProperties bottom) {
            this.bottom = bottom;
            return this;
        }

        public Builder left(FaceProperties left) {
            this.left = left;
            return this;
        }

        public Builder right(FaceProperties right) {
            this.right = right;
            return this;
        }

        public Builder front(FaceProperties front) {
            this.front = front;
            return this;
        }

        public Builder back(FaceProperties back) {
            this.back = back;
            return this;
        }

        public ModelProperties build() {
            return new ModelProperties(this.top, this.bottom, this.left, this.right, this.front, this.back);
        }
    }
}
