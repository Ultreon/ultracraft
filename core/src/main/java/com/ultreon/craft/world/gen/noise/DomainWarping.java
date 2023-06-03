package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.util.Mth;

public class DomainWarping {
    public final NoiseSettings noiseDomainX, noiseDomainY;
    public int amplitudeX = 20, amplitudeY = 20;

    public DomainWarping(NoiseSettings noiseDomainX, NoiseSettings noiseDomainY) {
        this.noiseDomainX = noiseDomainX;
        this.noiseDomainY = noiseDomainY;
    }

    public float generateDomainNoise(int x, int z, NoiseSettings defaultNoiseSettings) {
        Vector2 domainOffset = generateDomainOffset(x, z);
        return MyNoise.octavePerlin(x + domainOffset.x, z + domainOffset.y, noiseDomainX);
    }

    public Vector2 generateDomainOffset(int x, int z) {
        var noiseX = MyNoise.octavePerlin(x, z, noiseDomainX) * amplitudeX;
        var noiseY = MyNoise.octavePerlin(x, z, noiseDomainY) * amplitudeY;
        return new Vector2(noiseX, noiseY);
    }

    public GridPoint2 generateDomainOffsetInt(int x, int z) {
        return Mth.round(generateDomainOffset(x, z));
    }
}
