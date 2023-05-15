package com.ultreon.craft.world.gen;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BiomeCenterFinder {
    public static List<GridPoint2> neighbours8Direction = List.of(
            new GridPoint2(0, 1),
            new GridPoint2(1, 1),
            new GridPoint2(1, 0),
            new GridPoint2(1, -1),
            new GridPoint2(0, -1),
            new GridPoint2(-1, -1),
            new GridPoint2(-1, 0),
            new GridPoint2(-1, 1)
    );

    public static List<GridPoint3> CalculatedBiomeCenters(Vector3 playerPosition, int drawRange, int chunkSize) {
        int biomeLength = drawRange * chunkSize;

        GridPoint3 origin = new GridPoint3(MathUtils.round(playerPosition.x / biomeLength) * biomeLength, 0, MathUtils.round(playerPosition.y / biomeLength));
        Set<GridPoint3> biomeCentersTemp = new HashSet<>();

        biomeCentersTemp.add(origin);

        for (GridPoint2 offsetXZ : neighbours8Direction) {
            GridPoint3 newBiomePoint1 = new GridPoint3(origin.x + offsetXZ.x * biomeLength, 0, origin.z + offsetXZ.y * biomeLength);
            GridPoint3 newBiomePoint2 = new GridPoint3(origin.x + offsetXZ.x * biomeLength, 0, origin.z + offsetXZ.y * 2 * biomeLength);
            GridPoint3 newBiomePoint3 = new GridPoint3(origin.x + offsetXZ.x * 2 * biomeLength, 0, origin.z + offsetXZ.y * biomeLength);
            GridPoint3 newBiomePoint4 = new GridPoint3(origin.x + offsetXZ.x * 2 * biomeLength, 0, origin.z + offsetXZ.y * 2 * biomeLength);

            biomeCentersTemp.add(newBiomePoint1);
            biomeCentersTemp.add(newBiomePoint2);
            biomeCentersTemp.add(newBiomePoint3);
            biomeCentersTemp.add(newBiomePoint4);
        }

        return Lists.newArrayList(biomeCentersTemp);
    }
}
