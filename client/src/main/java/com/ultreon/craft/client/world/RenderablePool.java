package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.ultreon.craft.debug.ValueTracker;

public class RenderablePool extends FlushablePool<Renderable> {
    @Override
    protected Renderable newObject () {
        return new Renderable();
    }

    @Override
    public Renderable obtain () {
        Renderable renderable = super.obtain();
        renderable.environment = null;
        renderable.material = null;
        renderable.meshPart.set("", null, 0, 0, 0);
        renderable.shader = null;
        renderable.userData = null;

        ValueTracker.trackObtainRequest();

        return renderable;
    }

    public int getObtainedCount() {
        return this.obtained.size;
    }

    @Override
    public void free(Renderable object) {
        super.free(object);

        ValueTracker.trackFreeRequest();
    }

    @Override
    public void freeAll(Array<Renderable> objects) {
        super.freeAll(objects);

        ValueTracker.trackFreeRequests(objects.size);
    }

    @Override
    public void flush() {
        ValueTracker.trackFreeRequests(this.obtained.size);

        super.flush();

        ValueTracker.trackFlushRequest();
    }
}
