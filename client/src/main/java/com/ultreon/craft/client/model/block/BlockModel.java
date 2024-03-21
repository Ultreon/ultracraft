package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.client.resources.LoadableResource;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.world.BlockPos;

@SuppressWarnings("unused")
public interface BlockModel extends Disposable, LoadableResource {

    Vector3 DEFAULT_ITEM_SCALE = new Vector3(1, 1, 1);

    boolean isCustom();

    default void render(Vector3 pos, Array<Renderable> output, Pool<Renderable> renderablePool) {
        // Do nothing
    }

    default void loadInto(BlockPos pos, ClientChunk chunk) {
        // Do nothing
    }

    Model getModel();

    default Vector3 getItemScale() {
        return DEFAULT_ITEM_SCALE;
    }

    default Vector3 getItemOffset() {
        return Vector3.Zero;
    }
}
