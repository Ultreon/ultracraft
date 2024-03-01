package com.ultreon.craft.world;

/**
 * Represents a region position in the world.
 *
 * @param x the x position of the region.
 * @param y the y position of the region.
 * @param z the z position of the region.
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public record RegionPos(int x, int y, int z) {
    @Deprecated
    public RegionPos(int x, int z) {
        this(x, 0, z);
    }

    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.z;
    }
}
