package com.ultreon.craft.debug;

import com.ultreon.craft.world.gen.layer.TerrainLayer;

import java.util.HashSet;
import java.util.Set;

public class Debugger {
    public static Set<TerrainLayer> layersHandled = new HashSet<>();
    public static Set<TerrainLayer> layersTriggered = new HashSet<>();
    public static int maxY = Integer.MIN_VALUE;
    public static float highestSurface;
    public static double evaluatedMax;
    public static float octaveMax;
    public static float terrainMax;
    public static float redistMax;
    public static int stoneLayerMax = Integer.MIN_VALUE;
    public static int stoneLayerMin = Integer.MAX_VALUE;

    public static void dumpLayerInfo() {
        System.out.println("\nTriggered layers:");
        for (TerrainLayer terrainLayer : layersTriggered) {
            String name = terrainLayer.getClass().getName();
            System.out.println(" +-> " + name);
        }
        System.out.println("\nHandled layers:");
        for (TerrainLayer terrainLayer : layersHandled) {
            String name = terrainLayer.getClass().getName();
            System.out.println(" +-> " + name);
        }

        System.out.println("\nMaximum Y = " + maxY);
        System.out.println("Highest Gen. Y = " + highestSurface);
        System.out.println("Evaluated Max = " + evaluatedMax);
        System.out.println("Octave Max = " + octaveMax);
        System.out.println("Terrain Max = " + terrainMax);
        System.out.println("Redistribution Max = " + redistMax);
        System.out.println("Stone Layer Max = " + stoneLayerMax);
        System.out.println("Stone Layer Min = " + stoneLayerMin);
    }
}
