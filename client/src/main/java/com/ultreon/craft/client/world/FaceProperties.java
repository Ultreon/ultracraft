package com.ultreon.craft.client.world;

import java.util.Objects;

public class FaceProperties {
    public boolean randomRotation = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        FaceProperties that = (FaceProperties) o;
        return this.randomRotation == that.randomRotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.randomRotation);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FaceProperties faceProperties = new FaceProperties();

        public Builder randomRotation() {
            this.faceProperties.randomRotation = true;
            return this;
        }

        public FaceProperties build() {
            return this.faceProperties;
        }
    }
}
