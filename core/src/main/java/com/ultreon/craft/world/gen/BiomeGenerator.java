package com.ultreon.craft.world.gen;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseUtils;
import com.ultreon.craft.world.gen.noise.NoiseInstance;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class BiomeGenerator implements Disposable {
    private final World world;
    private final NoiseInstance biomeNoise;
    private final DomainWarping domainWarping;
    private final List<TerrainLayer> layers;
    private final List<TerrainLayer> extraLayers;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;

    public BiomeGenerator(World world, NoiseInstance noise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers) {
        this.world = world;
        this.biomeNoise = noise;
        this.domainWarping = domainWarping;
        this.layers = layers;
        this.extraLayers = extraLayers;
    }

    public Chunk processColumn(Chunk chunk, int x, int z, @Nullable Integer height) {
        final int chunkAmplitude = 1;

        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z, chunk.height) * chunkAmplitude;

        for (int y = chunk.getOffset().y; y < chunk.getOffset().y + chunk.height; y++) {
            for (TerrainLayer layer : this.layers) {
                if (layer.handle(this.world, chunk, x, y, z, groundPos)) {
                    break;
                }
            }
        }

        for (TerrainLayer layer : this.extraLayers) {
            layer.handle(this.world, chunk, x, chunk.getOffset().y, z, groundPos);
        }

        return chunk;
    }

    public int getSurfaceHeightNoise(float x, float z, int height) {
        float terrainHeight;
        if (!USE_DOMAIN_WARPING) {
            terrainHeight = NoiseUtils.octavePerlin(x, z, this.biomeNoise);
        } else {
            terrainHeight = this.domainWarping.generateDomainNoise((int) x, (int) z, this.biomeNoise);
        }

        terrainHeight = NoiseUtils.redistribution(terrainHeight, this.biomeNoise);
        return NoiseUtils.remapValue01ToInt(terrainHeight, 0, height);
    }

    public TreeData getTreeData(Chunk chunk, long seed) {
        if (treeGenerator == null)
            return new TreeData();

        return treeGenerator.generateTreeData(chunk, seed);
    }

    @Override
    public void dispose() {
        this.biomeNoise.dispose();
    }

    public World getWorld() {
        return this.world;
    }
}
