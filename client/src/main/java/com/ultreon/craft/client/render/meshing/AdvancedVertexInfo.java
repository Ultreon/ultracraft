package com.ultreon.craft.client.render.meshing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.libs.commons.v0.Mth;

public class AdvancedVertexInfo extends MeshPartBuilder.VertexInfo {
    public GreedyMesher.LightLevelData lightLevelData;
    public boolean hasLightLevelData;

    public AdvancedVertexInfo() {
        super();
        this.lightLevelData = null;
        this.hasLightLevelData = false;
    }

    public AdvancedVertexInfo setLLD(GreedyMesher.LightLevelData lld) {
        this.lightLevelData = lld;
        this.hasLightLevelData = true;
        return this;
    }

    public GreedyMesher.LightLevelData getLLD() {
        return this.lightLevelData;
    }

    public boolean hasLLD() {
        return this.hasLightLevelData;
    }

    @Override
    public AdvancedVertexInfo set(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
        return (AdvancedVertexInfo) super.set(pos, nor, col, uv);
    }

    @Override
    public AdvancedVertexInfo set(MeshPartBuilder.VertexInfo other) {
        if (other == null) return this.set(null, null, null, null);
        if (other instanceof AdvancedVertexInfo info) {
            this.lightLevelData = info.lightLevelData;
            return (AdvancedVertexInfo) super.set(other);
        }
        return (AdvancedVertexInfo) super.set(other);
    }

    @Override
    public AdvancedVertexInfo setPos(float x, float y, float z) {
        return (AdvancedVertexInfo) super.setPos(x, y, z);
    }

    @Override
    public AdvancedVertexInfo setPos(Vector3 pos) {
        return (AdvancedVertexInfo) super.setPos(pos);
    }

    @Override
    public AdvancedVertexInfo setNor(float x, float y, float z) {
        return (AdvancedVertexInfo) super.setNor(x, y, z);
    }

    @Override
    public AdvancedVertexInfo setNor(Vector3 nor) {
        return (AdvancedVertexInfo) super.setNor(nor);
    }

    @Override
    public AdvancedVertexInfo setCol(float r, float g, float b, float a) {
        return (AdvancedVertexInfo) super.setCol(r, g, b, a);
    }

    @Override
    public AdvancedVertexInfo setCol(Color col) {
        return (AdvancedVertexInfo) super.setCol(col);
    }

    @Override
    public AdvancedVertexInfo setUV(float u, float v) {
        return (AdvancedVertexInfo) super.setUV(u, v);
    }

    @Override
    public AdvancedVertexInfo setUV(Vector2 uv) {
        return (AdvancedVertexInfo) super.setUV(uv);
    }

    @Override
    public AdvancedVertexInfo lerp(MeshPartBuilder.VertexInfo target, float alpha) {
        if (target == null) return this.lerp(null, alpha);
        if (target instanceof AdvancedVertexInfo info) {
            double blockLerp = Mth.lerp(this.lightLevelData.blockBrightness(), info.lightLevelData.blockBrightness(), alpha);
            double sunLerp = Mth.lerp(this.lightLevelData.sunBrightness(), info.lightLevelData.sunBrightness(), alpha);
            AdvancedVertexInfo infoLerp = (AdvancedVertexInfo) super.lerp(target, alpha);
            infoLerp.lightLevelData = new GreedyMesher.LightLevelData((float) blockLerp, (float) sunLerp);
            return info;
        }
        return (AdvancedVertexInfo) super.lerp(target, alpha);
    }
}
