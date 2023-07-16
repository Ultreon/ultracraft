package com.ultreon.craft.world.gen.noise;

import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec2i;

public class DomainWarping {
    public final NoiseInstance domainX;
    public final NoiseInstance domainY;
    public int amplitudeX = 20, amplitudeY = 20;

    public DomainWarping(NoiseInstance domainX, NoiseInstance domainY) {
        this.domainX = domainX;
        this.domainY = domainY;
    }

    public double generateDomainNoise(int x, int z, NoiseInstance defaultNoiseSettings) {
        Vec2d domainOffset = this.generateDomainOffset(x, z);
        return MyNoise.octavePerlin(x + domainOffset.x, z + domainOffset.y, this.domainX);
    }

    public Vec2d generateDomainOffset(int x, int z) {
        double noiseX = MyNoise.octavePerlin(x, z, this.domainX) * this.amplitudeX;
        double noiseY = MyNoise.octavePerlin(x, z, this.domainY) * this.amplitudeY;
        return new Vec2d(noiseX, noiseY);
    }

    public Vec2i generateDomainOffsetInt(int x, int z) {
        return this.generateDomainOffset(x, z).i();
    }

    public void dispose() {
        this.domainX.dispose();
        this.domainY.dispose();
    }
}
