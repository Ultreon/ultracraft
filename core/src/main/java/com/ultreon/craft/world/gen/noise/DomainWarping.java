package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.util.MathHelper;

public class DomainWarping {
    public final NoiseInstance domainX;
    public final NoiseInstance domainY;
    public int amplitudeX = 20, amplitudeY = 20;

    public DomainWarping(NoiseInstance domainX, NoiseInstance domainY) {
        this.domainX = domainX;
        this.domainY = domainY;
    }

    public float generateDomainNoise(int x, int z, NoiseInstance defaultNoiseSettings) {
        Vector2 domainOffset = this.generateDomainOffset(x, z);
        float domainTotal = NoiseUtils.octavePerlin(x + domainOffset.x, z + domainOffset.y, this.domainX);
        System.out.println("domainTotal = " + domainTotal);
        return domainTotal;
    }

    public Vector2 generateDomainOffset(int x, int z) {
        float noiseX = NoiseUtils.octavePerlin(x, z, this.domainX) * this.amplitudeX;
        float noiseY = NoiseUtils.octavePerlin(x, z, this.domainY) * this.amplitudeY;
        return new Vector2(noiseX, noiseY);
    }

    public GridPoint2 generateDomainOffsetInt(int x, int z) {
        return MathHelper.round(this.generateDomainOffset(x, z));
    }
}
