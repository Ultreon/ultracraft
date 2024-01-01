package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.ultreon.craft.client.render.meshing.PerCornerLightData;

public interface BlockRenderer {

    void renderNorth(TextureRegion region, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData lightData, MeshBuilder builder);

    void renderSouth(TextureRegion region, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData lightData, MeshBuilder builder);

    void renderWest(TextureRegion region, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData lightData, MeshBuilder builder);

    void renderEast(TextureRegion region, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData lightData, MeshBuilder builder);

    void renderTop(TextureRegion region, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData lightData, MeshBuilder builder);

    void renderBottom(TextureRegion region, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData lightData, MeshBuilder builder);
}