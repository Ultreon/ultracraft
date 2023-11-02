package com.ultreon.craft.world.gen.trees;

import com.ultreon.libs.commons.v0.vector.Vec2f;
import it.unimi.dsi.fastutil.floats.Float2BooleanFunction;

import java.util.ArrayList;
import java.util.List;

public class DataProcessing {
    public static List<Vec2f> directions = List.of(new Vec2f(0, 1), //N
            new Vec2f(1, 1), //NE
            new Vec2f(1, 0), //E
            new Vec2f(-1, 1), //SE
            new Vec2f(-1, 0), //S
            new Vec2f(-1, -1), //SW
            new Vec2f(0, -1), //W
            new Vec2f(1, -1)  //NW
    );

    public static List<Vec2f> findLocalMaxima(float[][] dataMatrix, int xCoord, int zCoord) {
        List<Vec2f> maximas = new ArrayList<>();
        for (int x = 0; x < dataMatrix.length; x++) {
            for (int y = 0; y < dataMatrix[x].length; y++) {
                float noiseVal = dataMatrix[x][y];
                if (DataProcessing.checkNeighbours(dataMatrix, x, y, (neighbourNoise) -> neighbourNoise < noiseVal)) {
                    maximas.add(new Vec2f(xCoord + x, zCoord + y));
                }

            }
        }
        return maximas;
    }

    private static boolean checkNeighbours(float[][] dataMatrix, int x, int y, Float2BooleanFunction successCondition) {
        for (Vec2f dir : DataProcessing.directions) {
            Vec2f newPost = new Vec2f(x + dir.x, y + dir.y);

            if (newPost.x < 0 || newPost.x >= dataMatrix.length || newPost.y < 0 || newPost.y >= dataMatrix[0].length) {
                continue;
            }

            if (successCondition.get(dataMatrix[(int) (x + dir.x)][(int) (y + dir.y)])) {
                return false;
            }
        }
        return true;
    }

}