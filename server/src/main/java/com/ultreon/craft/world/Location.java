package com.ultreon.craft.world;

import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.ElementID;

/**
 * Represents a location in the world.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see World
 */
public class Location {
    /**
     * The world this location is in.
     */
    public ElementID world;

    /**
     * The coordinates of this location.
     */
    public double x;

    /**
     * The coordinates of this location.
     */
    public double y;

    /**
     * The coordinates of this location.
     */
    public double z;

    /**
     * The rotation in the world.
     */
    public float xRot;

    /**
     * The rotation in the world.
     */
    public float yRot;

    /**
     * Creates a new location.
     * 
     * @param world the world's id
     * @param x     the x-x coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param xRot  the x-rotation
     * @param yRot  the y-rotation
     */
    public Location(ElementID world, double x, double y, double z, float xRot, float yRot) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    /**
     * Creates a new location.
     * 
     * @param world the world
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param xRot  the x-rotation
     * @param yRot  the y-rotation
     */
    public Location(World world, double x, double y, double z, float xRot, float yRot) {
        this(world.getDimension().getId(), x, y, z, xRot, yRot);
    }

    /**
     * Creates a new location.
     * 
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @param z    the z-coordinate
     * @param xRot the x-rotation
     * @param yRot the y-rotation
     */
    public Location(double x, double y, double z, float xRot, float yRot) {
        this((ElementID) null, x, y, z, xRot, yRot);
    }

    /**
     * Creates a new location.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    public Location(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    /**
     * Creates a new location.
     * 
     * @param dimension the dimension the location is in
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    public Location(ServerWorld dimension, double x, double y, double z) {
        this(dimension, x, y, z, 0, 0);
    }

    /**
     * Creates a copy of this location.
     * 
     * @return the copy
     */
    public Location cpy() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    /**
     * Gets the server world.
     * 
     * @return the server world
     */
    public ServerWorld getSeverWorld() {
        UltracraftServer server = UltracraftServer.get();
        if (server == null) return null;

        return server.getWorld(this.world);
    }

    /**
     * Gets the block position.
     * 
     * @return the block position
     * @see BlockPos to get the block position
     * @see Block    to get the block
     */
    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}
