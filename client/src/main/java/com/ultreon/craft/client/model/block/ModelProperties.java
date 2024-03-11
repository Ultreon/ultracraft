package com.ultreon.craft.client.model.block;

import com.ultreon.craft.client.world.FaceProperties;
import com.ultreon.craft.world.CubicDirection;

import java.util.Objects;

public class ModelProperties {
    public final FaceProperties top;
    public final FaceProperties bottom;
    public final FaceProperties left;
    public final FaceProperties right;
    public final FaceProperties front;
    public final FaceProperties back;
    public final CubicDirection rotation;

    public ModelProperties(FaceProperties top, FaceProperties bottom, FaceProperties left, FaceProperties right, FaceProperties front, FaceProperties back, CubicDirection rotation) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.rotation = rotation;
    }

    public ModelProperties(FaceProperties all, CubicDirection rotation) {
        this(all, all, all, all, all, all, rotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ModelProperties that = (ModelProperties) o;
        return Objects.equals(this.top, that.top) && Objects.equals(this.bottom, that.bottom) && Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right) && Objects.equals(this.front, that.front) && Objects.equals(this.back, that.back) && Objects.equals(this.rotation, that.rotation);
    }

    @Override
    public int hashCode() {
        int result = this.top.hashCode();
        result = 31 * result + this.bottom.hashCode();
        result = 31 * result + this.left.hashCode();
        result = 31 * result + this.right.hashCode();
        result = 31 * result + this.front.hashCode();
        result = 31 * result + this.back.hashCode();
        result = 31 * result + this.rotation.hashCode();
        return result;
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
        private CubicDirection horizontalRotation = CubicDirection.NORTH;

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
            return new ModelProperties(this.top, this.bottom, this.left, this.right, this.front, this.back, this.horizontalRotation);
        }

        public Builder rotateHorizontal(CubicDirection direction) {
            this.horizontalRotation = direction;
            return this;
        }
    }
}
