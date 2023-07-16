package com.ultreon.craft.world.gen;

import com.badlogic.gdx.math.GridPoint2;
import com.google.common.collect.Lists;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.*;

public class BiomeCenterFinder {
    public static List<GridPoint2> neighbours8Direction = Arrays.asList(
            new GridPoint2(0, 1),
            new GridPoint2(1, 1),
            new GridPoint2(1, 0),
            new GridPoint2(1, -1),
            new GridPoint2(0, -1),
            new GridPoint2(-1, -1),
            new GridPoint2(-1, 0),
            new GridPoint2(-1, 1)
    );

    public static ArrayList<Vec3i> calcBiomeCenters(Vec3d playerPosition, int drawRange, int chunkSize) {
        Vec3i origin = new Vec3i((int) (Math.round(playerPosition.x / chunkSize) * chunkSize), 0, (int) Math.round(playerPosition.z / chunkSize));
        Set<Vec3i> biomeCentersTemp = new HashSet<>();

        biomeCentersTemp.add(origin);

        for (GridPoint2 offsetXZ : neighbours8Direction) {
            Vec3i newBiomePoint1 = new Vec3i(origin.x + offsetXZ.x * chunkSize, 0, origin.z + offsetXZ.y * chunkSize);
            Vec3i newBiomePoint2 = new Vec3i(origin.x + offsetXZ.x * chunkSize, 0, origin.z + offsetXZ.y * 2 * chunkSize);
            Vec3i newBiomePoint3 = new Vec3i(origin.x + offsetXZ.x * 2 * chunkSize, 0, origin.z + offsetXZ.y * chunkSize);
            Vec3i newBiomePoint4 = new Vec3i(origin.x + offsetXZ.x * 2 * chunkSize, 0, origin.z + offsetXZ.y * 2 * chunkSize);

            biomeCentersTemp.add(newBiomePoint1);
            biomeCentersTemp.add(newBiomePoint2);
            biomeCentersTemp.add(newBiomePoint3);
            biomeCentersTemp.add(newBiomePoint4);
        }

        return Lists.newArrayList(biomeCentersTemp);
    }
}
