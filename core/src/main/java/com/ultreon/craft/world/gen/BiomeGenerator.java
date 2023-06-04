package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.RawChunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.feature.Feature;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.MyNoise;
import com.ultreon.craft.world.gen.noise.NoiseSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BiomeGenerator {
    private final NoiseSettings biomeNoise;
    private final DomainWarping domainWarping;
    private final List<TerrainLayer> layers;
    private final List<TerrainLayer> extraLayers;
    private final List<Feature> features;
    public static final boolean USE_DOMAIN_WARPING = true;
    public TreeGenerator treeGenerator;
    public FeatureGenData featureGenData = new FeatureGenData();

    @Deprecated
    public BiomeGenerator(NoiseSettings biomeNoise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers) {
        this(biomeNoise, domainWarping, layers, extraLayers, new ArrayList<>());
    }

    public BiomeGenerator(NoiseSettings biomeNoise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers, List<Feature> features) {
        this.biomeNoise = biomeNoise;
        this.domainWarping = domainWarping;
        this.layers = layers;
        this.features = features;
        System.out.println("layers = " + layers);
        this.extraLayers = extraLayers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RawChunk processColumn(World world, RawChunk chunk, int x, int z, long seed) {
        this.biomeNoise.setSeed(seed);
        this.domainWarping.noiseDomainX.setSeed(seed);
        this.domainWarping.noiseDomainY.setSeed(seed);

        final int chunkAmplitude = 1;
        int groundPos = getSurfaceHeightNoise(chunk.offset.x + x, chunk.offset.z + z, chunk.height) * chunkAmplitude;

        for (int y = chunk.offset.y; y < chunk.offset.y + chunk.height; y++) {
            for (var layer : this.layers) {
                if (layer.handle(chunk, x, y, z, groundPos, seed)) {
                    break;
                }
            }
        }

        for (var layer : this.extraLayers) {
            layer.handle(chunk, x, chunk.offset.y, z, groundPos, seed);
        }

        Map<Integer, List<Feature>> map = features.stream().collect(Collectors.groupingBy((p_211653_) -> p_211653_.stage().ordinal()));

        WorldGenRandom random = new WorldGenRandom(seed);
        long decoStageSeed = random.setDecoStageSeed(seed, x, z);
        int i = 0;
        for (int stage = 0; stage < GenerationStage.values().length; stage++) {
            for (int y = chunk.offset.y; y < chunk.offset.y + chunk.height; y++) {
                for (var feature : this.features) {
                    random.setFeatureSeed(decoStageSeed, i, stage);
                    if (feature.canGenerate(world, chunk, x, y, z, random)) {
                        feature.generate(world, chunk, x, y, z, random);
                    }
                    i++;
                }
            }
        }

        return chunk;
    }

    public int getSurfaceHeightNoise(float x, float z, int height) {
        float terrainHeight;
        if (!USE_DOMAIN_WARPING) {
            terrainHeight = MyNoise.octavePerlin(x, z, biomeNoise);
        } else {
            terrainHeight = domainWarping.generateDomainNoise((int) x, (int) z, biomeNoise);
        }

        terrainHeight = MyNoise.redistribution(terrainHeight, biomeNoise);
        return MyNoise.remapValue01ToInt(terrainHeight, 0, height);
//        return (int) (terrainHeight * height);
    }

    @Deprecated
    public TreeData getTreeData(RawChunk chunk, long seed) {
        if (treeGenerator == null)
            return new TreeData();

        return treeGenerator.generateTreeData(chunk, seed);
    }

    public static class Builder {
        private NoiseSettings biomeNoise;
        private DomainWarping domainWarping;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainLayer> extraLayers = new ArrayList<>();
        private final List<Feature> features = new ArrayList<>();

        public Builder noise(NoiseSettings biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        public Builder domainWarping(DomainWarping domainWarping) {
            this.domainWarping = domainWarping;
            return this;
        }

        public Builder layer(TerrainLayer layer) {
            this.layers.add(layer);
            return this;
        }

        public Builder extraLayer(TerrainLayer layer) {
            this.extraLayers.add(layer);
            return this;
        }

        public Builder feature(Feature feature) {
            this.features.add(feature);
            return this;
        }

        public BiomeGenerator build() {
            return new BiomeGenerator(biomeNoise, domainWarping, layers, extraLayers, features);
        }
    }
}
