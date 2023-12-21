package com.ultreon.craft.world.gen.carver;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import de.articdive.jnoise.generators.noisegen.worley.WorleyNoiseGenerator;

import java.util.UUID;

public class CaveOverworldCarver implements CarverType {
    private static final UUID CAVE_NOISE_KEY = UUID.fromString("e6c07705-ff0a-452b-9437-fddc2cf9dc4a");

    public CaveOverworldCarver() {
        // TODO document why this constructor is empty
    }

    @Override
    public void carve(ServerWorld world, BuilderChunk chunk, Carver instance, int x, int y, int z) {
        if (y == 0) {
            world.set(x, y, z, Blocks.VOIDGUARD);
            return;
        }

        if (y > 192) {
            world.set(x, y, z, Blocks.AIR);
            return;
        }

        double height = instance.getNoise().eval(x, z) + 64;

        if (y < height) {
            chunk.set(x, y, z, Blocks.STONE);
            return;
        }

        chunk.set(x, y, z, Blocks.AIR);

//        double caveCarve = instance.get(CaveOverworldCarver.CAVE_NOISE_KEY).eval(x, y, z);
//        if (caveCarve < 0.3 && height <= y) {
//            world.set(x, y, z, Blocks.CAVE_AIR);
//        }
    }

    @Override
    public Carver create(long seed) {
        Carver carver = new Carver(this, NoiseConfigs.OVERWORLD_NOISE.create(seed));
        carver.set(CaveOverworldCarver.CAVE_NOISE_KEY, NoiseConfigs.CAVE_NOISE.create(seed, v -> WorleyNoiseGenerator.newBuilder().setSeed(v).setDepth(10).build()));
        return carver;
    }
}
