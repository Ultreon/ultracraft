package com.ultreon.craft.client;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.math.*;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.world.ChunkMesh;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.debug.inspect.DefaultInspections;
import com.ultreon.craft.debug.inspect.InspectionRoot;

public class DebugRegistration {
    public static void registerAutoFillers() {
        InspectionRoot.registerAutoFill(ClientChunk.class, node -> {
            node.createNode("mesh", value -> value.solidMesh);
            node.createNode("transparentMesh", value -> value.transparentMesh);
            node.createNode("dirty", value -> value.dirty);
            node.createNode("renderOffset", value -> value.renderOffset);
        });

        InspectionRoot.registerAutoFill(ClientWorld.class, node -> node.create("renderDistance", ClientWorld::getRenderDistance));
        InspectionRoot.registerAutoFill(Graphics.DisplayMode.class, node -> {
            node.create("width", n -> n.width);
            node.create("height", n -> n.height);
            node.create("hertz", n -> n.refreshRate);
            node.create("bitsPerPixel", n -> n.bitsPerPixel);
        });

        InspectionRoot.registerAutoFill(Graphics.Monitor.class, node -> {
            node.create("name", n -> n.name);
            node.create("x", n -> n.virtualX);
            node.create("y", n -> n.virtualY);
        });

        InspectionRoot.registerAutoFill(Graphics.BufferFormat.class, node -> {
            node.create("red", n -> n.r);
            node.create("green", n -> n.g);
            node.create("blue", n -> n.b);
            node.create("alpha", n -> n.a);
            node.create("depth", n -> n.depth);
            node.create("stencil", n -> n.stencil);
            node.create("samples", n -> n.samples);
            node.create("coverageSamples", n -> n.coverageSampling);
        });

        InspectionRoot.registerFormatter(boolean.class, element -> Boolean.toString(element));
        InspectionRoot.registerFormatter(void.class, element -> "void");
        InspectionRoot.registerFormatter(byte.class, element -> Byte.toString(element));
        InspectionRoot.registerFormatter(short.class, element -> Short.toString(element));
        InspectionRoot.registerFormatter(int.class, element -> Integer.toString(element));
        InspectionRoot.registerFormatter(long.class, element -> Long.toString(element));
        InspectionRoot.registerFormatter(float.class, element -> Float.toString(element));
        InspectionRoot.registerFormatter(double.class, element -> Double.toString(element));
        InspectionRoot.registerFormatter(char.class, element -> Character.toString(element));
        InspectionRoot.registerFormatter(String.class, element -> "\"" + element + "\"");
        InspectionRoot.registerFormatter(IntegratedServer.class, element -> "integratedServer");
        InspectionRoot.registerFormatter(ClientWorld.class, element -> "clientWorld");
        InspectionRoot.registerFormatter(ClientPlayer.class, element -> "clientPlayer[" + element.getUuid() + "]");
        InspectionRoot.registerFormatter(ClientChunk.class, element -> "clientChunk[" + element.getPos() + "]");
        InspectionRoot.registerFormatter(ChunkMesh.class, element -> "chunkMesh[" + element.meshPart.mesh.getVertexSize() + "]");
        InspectionRoot.registerFormatter(Mesh.class, element -> "mesh[" + element.getVertexSize() + "]");
        InspectionRoot.registerFormatter(MeshPart.class, element -> "meshPart[" + element.mesh.getVertexSize() + "]");
        InspectionRoot.registerFormatter(TextureRegion.class, element -> "textureRegion[" + element.getRegionWidth() + "x" + element.getRegionHeight() + "]");
        InspectionRoot.registerFormatter(Texture.class, element -> "texture[" + element.getWidth() + "x" + element.getHeight() + "]");
        InspectionRoot.registerFormatter(Vector2.class, element -> "vector2(" + element.x + ", " + element.y + ")");
        InspectionRoot.registerFormatter(Vector3.class, element -> "vector3(" + element.x + ", " + element.y + ", " + element.z + ")");
        InspectionRoot.registerFormatter(Quaternion.class, element -> "quaternion(" + element.x + ", " + element.y + ", " + element.z + ", " + element.w + ")");
        InspectionRoot.registerFormatter(Color.class, element -> "rgba(" + element.r + ", " + element.g + ", " + element.b + ", " + element.a + ")");
        InspectionRoot.registerFormatter(Circle.class, element -> "circle(" + element.x + ", " + element.y + ", rad=" + element.radius + ")");
        InspectionRoot.registerFormatter(Rectangle.class, element -> "rectangle(" + element.x + ", " + element.y + " + " + element.width + "x" + element.height + ")");
        InspectionRoot.registerFormatter(Ellipse.class, element -> "ellipse(" + element.x + ", " + element.y + " + " + element.width + "x" + element.height + ")");
        InspectionRoot.registerFormatter(GridPoint2.class, element -> "gridPoint2(" + element.x + ", " + element.y + ")");
        InspectionRoot.registerFormatter(GridPoint3.class, element -> "gridPoint3(" + element.x + ", " + element.y + ", " + element.z + ")");
        InspectionRoot.registerFormatter(GLVersion.class, glVersion -> glVersion.getType() + " " + glVersion.getMajorVersion() + "." + glVersion.getMinorVersion() + "." + glVersion.getReleaseVersion());

        DefaultInspections.register();
    }
}
