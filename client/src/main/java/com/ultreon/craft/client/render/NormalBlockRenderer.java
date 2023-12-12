package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.ultreon.craft.client.render.meshing.PerCornerLightData;
import com.ultreon.craft.util.Color;

public class NormalBlockRenderer implements BlockRenderer {
    private static final float TEXTURE_PERCENTAGE = 16f / 2048f;
    private static final int BLOCKS_PER_WIDTH = 2048 / 16;

    private final VertexInfo c00 = new VertexInfo();
    private final VertexInfo c01 = new VertexInfo();
    private final VertexInfo c10 = new VertexInfo();
    private final VertexInfo c11 = new VertexInfo();

    @Override
    public void renderNorth(TextureRegion region, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData lightData, MeshBuilder builder) {
        // POSITIVE Z
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        this.c00.setPos(x1, y1, z).setNor(0, 0, 1).setUV(getU(region), getV(region));
        this.c01.setPos(x1, y2, z).setNor(0, 0, 1).setUV(getU(region), getV(region));
        this.c10.setPos(x2, y1, z).setNor(0, 0, 1).setUV(getU(region), getV(region));
        this.c11.setPos(x2, y2, z).setNor(0, 0, 1).setUV(getU(region), getV(region));

        Color c = this.getColor((int) x1, (int) y1, (int) z - 1);
        if (lightData == null) {
            builder.setColor(c.getRed() * lightLevel, c.getGreen() * lightLevel, c.getBlue() * lightLevel, c.getAlpha());
        } else {
            this.c00.setCol(c.getRed() * lightData.l00, c.getGreen() * lightData.l00, c.getBlue() * lightData.l00, c.getAlpha());
            this.c01.setCol(c.getRed() * lightData.l01, c.getGreen() * lightData.l01, c.getBlue() * lightData.l01, c.getAlpha());
            this.c10.setCol(c.getRed() * lightData.l10, c.getGreen() * lightData.l10, c.getBlue() * lightData.l10, c.getAlpha());
            this.c11.setCol(c.getRed() * lightData.l11, c.getGreen() * lightData.l11, c.getBlue() * lightData.l11, c.getAlpha());
        }

        builder.rect(this.c00, this.c10, this.c11, this.c01);
    }

    @Override
    public void renderSouth(TextureRegion region, float x1, float y1, float x2, float y2, float z, float lightLevel, PerCornerLightData lightData, MeshBuilder builder) {
        // NEGATIVE Z
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        this.c00.setPos(x1, y1, z).setNor(0, 0, -1).setUV(getU(region), getV(region));
        this.c01.setPos(x1, y2, z).setNor(0, 0, -1).setUV(getU(region), getV(region));
        this.c10.setPos(x2, y1, z).setNor(0, 0, -1).setUV(getU(region), getV(region));
        this.c11.setPos(x2, y2, z).setNor(0, 0, -1).setUV(getU(region), getV(region));

        Color c = getColor((int) x1, (int) y1, (int) z);
        if (lightData == null) {
            builder.setColor(c.getRed() * lightLevel, c.getGreen() * lightLevel, c.getBlue() * lightLevel, c.getAlpha());
        } else {
            this.c00.setCol(c.getRed() * lightData.l00, c.getGreen() * lightData.l00, c.getBlue() * lightData.l00, c.getAlpha());
            this.c01.setCol(c.getRed() * lightData.l01, c.getGreen() * lightData.l01, c.getBlue() * lightData.l01, c.getAlpha());
            this.c10.setCol(c.getRed() * lightData.l10, c.getGreen() * lightData.l10, c.getBlue() * lightData.l10, c.getAlpha());
            this.c11.setCol(c.getRed() * lightData.l11, c.getGreen() * lightData.l11, c.getBlue() * lightData.l11, c.getAlpha());
        }

        builder.rect(this.c01, this.c11, this.c10, this.c00);
    }

    @Override
    public void renderWest(TextureRegion region, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData lightData, MeshBuilder builder) {
        // NEGATIVE X
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        this.c00.setPos(x, y1, z1).setNor(-1, 0, 0).setUV(getU(region), getV(region));
        this.c01.setPos(x, y1, z2).setNor(-1, 0, 0).setUV(getU(region), getV(region));
        this.c10.setPos(x, y2, z1).setNor(-1, 0, 0).setUV(getU(region), getV(region));
        this.c11.setPos(x, y2, z2).setNor(-1, 0, 0).setUV(getU(region), getV(region));

        Color c = getColor((int) x, (int) y1, (int) z1);
        if (lightData == null) {
            builder.setColor(c.getRed() * lightLevel, c.getGreen() * lightLevel, c.getBlue() * lightLevel, c.getAlpha());
        } else {
            this.c00.setCol(c.getRed() * lightData.l00, c.getGreen() * lightData.l00, c.getBlue() * lightData.l00, c.getAlpha());
            this.c01.setCol(c.getRed() * lightData.l01, c.getGreen() * lightData.l01, c.getBlue() * lightData.l01, c.getAlpha());
            this.c10.setCol(c.getRed() * lightData.l10, c.getGreen() * lightData.l10, c.getBlue() * lightData.l10, c.getAlpha());
            this.c11.setCol(c.getRed() * lightData.l11, c.getGreen() * lightData.l11, c.getBlue() * lightData.l11, c.getAlpha());
        }

        builder.rect(this.c01, this.c11, this.c10, this.c00);
    }

    @Override
    public void renderEast(TextureRegion region, float z1, float y1, float z2, float y2, float x, float lightLevel, PerCornerLightData lightData, MeshBuilder builder) {
        // POSITIVE X
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        this.c00.setPos(x, y1, z1).setNor(1, 0, 0).setUV(getU(region), getV(region));
        this.c01.setPos(x, y1, z2).setNor(1, 0, 0).setUV(getU(region), getV(region));
        this.c10.setPos(x, y2, z1).setNor(1, 0, 0).setUV(getU(region), getV(region));
        this.c11.setPos(x, y2, z2).setNor(1, 0, 0).setUV(getU(region), getV(region));

        Color c = this.getColor((int) x - 1, (int) y1, (int) z1);
        if (lightData == null) {
            builder.setColor(c.getRed() * lightLevel, c.getGreen() * lightLevel, c.getBlue() * lightLevel, c.getAlpha());
        } else {
            this.c00.setCol(c.getRed() * lightData.l00, c.getGreen() * lightData.l00, c.getBlue() * lightData.l00, c.getAlpha());
            this.c01.setCol(c.getRed() * lightData.l01, c.getGreen() * lightData.l01, c.getBlue() * lightData.l01, c.getAlpha());
            this.c10.setCol(c.getRed() * lightData.l10, c.getGreen() * lightData.l10, c.getBlue() * lightData.l10, c.getAlpha());
            this.c11.setCol(c.getRed() * lightData.l11, c.getGreen() * lightData.l11, c.getBlue() * lightData.l11, c.getAlpha());
        }

        builder.rect(this.c00, this.c10, this.c11, this.c01);
    }

    @Override
    public void renderTop(TextureRegion region, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData lightData, MeshBuilder builder) {
        // POSITIVE Y
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        this.c00.setPos(x1, y, z1).setNor(0, 1, 0).setUV(getU(region), getV(region));
        this.c01.setPos(x1, y, z2).setNor(0, 1, 0).setUV(getU(region), getV(region));
        this.c10.setPos(x2, y, z1).setNor(0, 1, 0).setUV(getU(region), getV(region));
        this.c11.setPos(x2, y, z2).setNor(0, 1, 0).setUV(getU(region), getV(region));

        Color c = this.getColor((int) x1, (int) y - 1, (int) z1);
        if (lightData == null) {
            builder.setColor(c.getRed() * lightLevel, c.getGreen() * lightLevel, c.getBlue() * lightLevel, c.getAlpha());
        } else {
            this.c00.setCol(c.getRed() * lightData.l00, c.getGreen() * lightData.l00, c.getBlue() * lightData.l00, c.getAlpha());
            this.c01.setCol(c.getRed() * lightData.l01, c.getGreen() * lightData.l01, c.getBlue() * lightData.l01, c.getAlpha());
            this.c10.setCol(c.getRed() * lightData.l10, c.getGreen() * lightData.l10, c.getBlue() * lightData.l10, c.getAlpha());
            this.c11.setCol(c.getRed() * lightData.l11, c.getGreen() * lightData.l11, c.getBlue() * lightData.l11, c.getAlpha());
        }

        builder.rect(this.c01, this.c11, this.c10, this.c00);
    }

    @Override
    public void renderBottom(TextureRegion region, float x1, float z1, float x2, float z2, float y, float lightLevel, PerCornerLightData lightData, MeshBuilder builder) {
        // NEGATIVE Y
        builder.setUVRange(NormalBlockRenderer.getU(region), NormalBlockRenderer.getV(region), NormalBlockRenderer.getU2(region), NormalBlockRenderer.getV2(region));

        this.c00.setPos(x1, y, z1).setNor(0, -1, 0).setUV(getU(region), getV(region));
        this.c01.setPos(x1, y, z2).setNor(0, -1, 0).setUV(getU(region), getV(region));
        this.c10.setPos(x2, y, z1).setNor(0, -1, 0).setUV(getU(region), getV(region));
        this.c11.setPos(x2, y, z2).setNor(0, -1, 0).setUV(getU(region), getV(region));

        Color c = this.getColor((int) x1, (int) y, (int) z1);
        if (lightData == null) {
            builder.setColor(c.getRed() * lightLevel, c.getGreen() * lightLevel, c.getBlue() * lightLevel, c.getAlpha());
        } else {
            this.c00.setCol(c.getRed() * lightData.l00, c.getGreen() * lightData.l00, c.getBlue() * lightData.l00, c.getAlpha());
            this.c01.setCol(c.getRed() * lightData.l01, c.getGreen() * lightData.l01, c.getBlue() * lightData.l01, c.getAlpha());
            this.c10.setCol(c.getRed() * lightData.l10, c.getGreen() * lightData.l10, c.getBlue() * lightData.l10, c.getAlpha());
            this.c11.setCol(c.getRed() * lightData.l11, c.getGreen() * lightData.l11, c.getBlue() * lightData.l11, c.getAlpha());
        }

        builder.rect(this.c00, this.c10, this.c11, this.c01);
    }

    protected Color getColor(int x, int y, int z) {
        return Color.WHITE;
    }

    private static void initialize() {
    }

    public static float getU(TextureRegion region) {
        return region.getU() / (1 + NormalBlockRenderer.TEXTURE_PERCENTAGE);
    }

    public static float getV(TextureRegion region) {
        return region.getV();
    }

    public static float getU2(TextureRegion region) {
        return region.getU2() / (1 + NormalBlockRenderer.TEXTURE_PERCENTAGE);
    }

    public static float getV2(TextureRegion region) {
        return region.getV2();
    }
}