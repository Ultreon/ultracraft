package com.ultreon.craft.client.render.meshing;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.ultreon.craft.block.Block;

/**
 * Turns an array of voxels into OpenGL vertices
 */
public interface Mesher {

    /**
     * Meshes the specified voxels.
     *
     * @param builder     MeshBuilder to build the mesh onto
     * @param condition   Condition to check if the block should be used in the mesh
     */
    Mesh meshVoxels(MeshBuilder builder, UseCondition condition);

    interface UseCondition {
        /**
         * @param block Block to check
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(Block block);
    }

}