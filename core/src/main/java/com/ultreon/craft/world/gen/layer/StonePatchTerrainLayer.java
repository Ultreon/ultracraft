package com.ultreon.craft.world.gen.layer;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettings;

public class StonePatchTerrainLayer extends TerrainLayer {
    public float stoneThreshold = 0.5f;

    public DomainWarping domainWarping;

    public StonePatchTerrainLayer(NoiseSettings noiseSettings) {

    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
//        if (chunk.offset.y > height)
//            return false;
//
//        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
//        float stoneNoise = this.domainWarping.generateDomainNoise(chunk.offset.x + x, chunk.offset.z + z, noiseSettings);
//
//        int endPosition = height;
//        if (chunk.offset.y < 0) {
//            endPosition = chunk.offset.y + chunk.height;
//        }
//
//        if (stoneNoise > this.stoneThreshold) {
//            for (int i = chunk.offset.y; i <= endPosition; i++) {
//                GridPoint3 pos = new GridPoint3(x, i, z);
//                try {
//                    chunk.set(pos, Blocks.STONE);
//                } catch (Exception e) {
////                    System.out.println("pos = " + pos);
//                    throw new RuntimeException("Execution error at " + pos, e);
//                }
//            }
//            return true;
//        }
        return false;
    }
}
