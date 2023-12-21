package com.ultreon.craft.world.gen.carver;

import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.gen.noise.NoiseInstance;

import java.util.IdentityHashMap;
import java.util.UUID;

public class Carver {
    private final CarverType type;
    private final NoiseInstance noise;
    private final IdentityHashMap<UUID, NoiseInstance> instances = new IdentityHashMap<>();

    public Carver(CarverType type, NoiseInstance noise) {
        this.type = type;
        this.noise = noise;
    }

    public void carve(ServerWorld world, BuilderChunk chunk, int x, int y, int z) {
        this.type.carve(world, chunk, this, x, y, z);
    }

    public void set(UUID key, NoiseInstance instance) {
        this.instances.put(key, instance);
    }

    public NoiseInstance get(UUID caveNoiseKey) {
        return this.instances.get(caveNoiseKey);
    }

    public NoiseInstance getNoise() {
        return this.noise;
    }
}
