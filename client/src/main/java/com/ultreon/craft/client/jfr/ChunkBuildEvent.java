package com.ultreon.craft.client.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;

@Label("Chunk Building")
@Category({"World Rendering", "Meshes"})
public class ChunkBuildEvent extends Event {
    @Label("Indices")
    public int indices;
    @Label("Vertices")
    public int vertices;
    @Label("Defined Indices")
    public int definedIndices;
    @Label("Defined Vertices")
    public int definedVertices;
    @Label("Max Indices")
    public int maxMeshIndices;
    @Label("Max Vertices")
    public int maxMeshVertices;
}
