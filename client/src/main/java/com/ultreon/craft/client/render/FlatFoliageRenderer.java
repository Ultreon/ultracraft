package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.ultreon.craft.client.render.meshing.GreedyMesher.LightLevelData;
import com.ultreon.craft.client.render.meshing.PerCornerLightData;

/**
 * Renders foliage blocks, such as tall grass, as a "+" made out of their textures.
 */
public class FlatFoliageRenderer extends NormalBlockRenderer {
    @Override
    public void renderNorth(TextureRegion region, float x1, float y1, float x2, float y2, float z, LightLevelData lld, PerCornerLightData lightData, MeshBuilder builder) {
        super.renderNorth(region, x1, y1, x2, y2, z - 0.5f, lld, lightData, builder);
    }

    @Override
    public void renderSouth(TextureRegion region, float x1, float y1, float x2, float y2, float z, LightLevelData lld, PerCornerLightData lightData, MeshBuilder builder) {
        super.renderSouth(region, x1, y1, x2, y2, z + 0.5f, lld, lightData, builder);
    }

    @Override
    public void renderWest(TextureRegion region, float z1, float y1, float z2, float y2, float x, LightLevelData lld, PerCornerLightData lightData, MeshBuilder builder) {
        super.renderWest(region, z1, y1, z2, y2, x + 0.5f, lld, lightData, builder);
    }

    @Override
    public void renderEast(TextureRegion region, float z1, float y1, float z2, float y2, float x, LightLevelData lld, PerCornerLightData lightData, MeshBuilder builder) {
        super.renderEast(region, z1, y1, z2, y2, x - 0.5f, lld, lightData, builder);
    }

    @Override
    public void renderTop(TextureRegion region, float x1, float z1, float x2, float z2, float y, LightLevelData lld, PerCornerLightData lightData, MeshBuilder builder) {}
    @Override
    public void renderBottom(TextureRegion region, float x1, float z1, float x2, float z2, float y, LightLevelData lld, PerCornerLightData lightData, MeshBuilder builder) {}

}
