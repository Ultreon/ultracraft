package com.ultreon.craft.world.gen;

public final class WorldGenSettings {
    private final int generationAmplitude;

    private WorldGenSettings(int generationAmplitude) {
        this.generationAmplitude = generationAmplitude;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int generationAmplitude() {
        return this.generationAmplitude;
    }

    @Override
    public String toString() {
        return "WorldGenSettings{" +
                "generationAmplitude=" + this.generationAmplitude +
                '}';
    }


    public static class Builder {
        private int generationAmplitude = 8;

        public WorldGenSettings build() {
            return new WorldGenSettings(this.generationAmplitude);
        }

        public Builder generationAmplitude(int generationAmplitude) {
            this.generationAmplitude = generationAmplitude;
            return this;
        }
    }
}
