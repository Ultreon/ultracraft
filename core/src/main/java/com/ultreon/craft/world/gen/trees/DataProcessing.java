package com.ultreon.craft.world.gen.trees;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.floats.Float2BooleanFunction;

public class DataProcessing {
    public static List<Vector2> directions = List.of(new Vector2(0, 1), //N
            new Vector2(1, 1), //NE
            new Vector2(1, 0), //E
            new Vector2(-1, 1), //SE
            new Vector2(-1, 0), //S
            new Vector2(-1, -1), //SW
            new Vector2(0, -1), //W
            new Vector2(1, -1)  //NW
    );

    public static List<Vector2> findLocalMaxima(float[][] dataMatrix, int xCoord, int zCoord) {
        List<Vector2> maximas = new ArrayList<>();
        for (int x = 0; x < dataMatrix.length; x++) {
            for (int y = 0; y < dataMatrix[x].length; y++) {
                float noiseVal = dataMatrix[x][y];
                if (checkNeighbours(dataMatrix, x, y, (neighbourNoise) -> neighbourNoise < noiseVal)) {
                    maximas.add(new Vector2(xCoord + x, zCoord + y));
                }

            }
        }
        return maximas;
    }

    private static boolean checkNeighbours(float[][] dataMatrix, int x, int y, Float2BooleanFunction successCondition) {
        for (Vector2 dir : directions) {
            Vector2 newPost = new Vector2(x + dir.x, y + dir.y);

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