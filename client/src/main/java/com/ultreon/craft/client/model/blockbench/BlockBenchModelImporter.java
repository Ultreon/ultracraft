package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.ModelImporter;
import com.ultreon.craft.client.util.Utils;
import com.ultreon.craft.resources.Resource;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.CubicDirection;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec4f;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class BlockBenchModelImporter implements ModelImporter {
    private final BBMeta meta;
    private final String name;
    private final String modelIdentifier;
    private final Vec3f visibleBox;
    private final Vec2f resolution;
    private final List<BBModelElement> elements;
    private final Identifier id;
    private final BBModelOutliner outliner;
    private final List<BBAnimation> animations;
    private final List<BBTexture> textures;
    private Model model;

    public BlockBenchModelImporter(Identifier id) {
        this.id = id;
        UltracraftClient client = UltracraftClient.get();
        Resource resource = client.getResourceManager().getResource(id);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + id);
        }

        JsonObject jsonObject = resource.loadJson(JsonObject.class);
        this.meta = loadMeta(jsonObject.getAsJsonObject("meta"));
        this.name = jsonObject.getAsJsonPrimitive("name").getAsString();
        this.modelIdentifier = jsonObject.getAsJsonPrimitive("model_identifier").getAsString();

        this.visibleBox = loadVec3(jsonObject.getAsJsonArray("visible_box"));

        this.resolution = loadVec2Size(jsonObject.getAsJsonObject("resolution"));
        this.elements = loadElements(jsonObject.getAsJsonArray("elements"));

        this.outliner = loadOutliner(jsonObject.getAsJsonArray("outliner"));
        this.textures = loadTextures(jsonObject.getAsJsonArray("textures"));
        this.animations = loadAnimations(jsonObject.getAsJsonArray("animations"));
    }

    private Vec2f loadVec2Size(JsonObject resolution) {
        return new Vec2f(resolution.get("width").getAsFloat(), resolution.get("height").getAsFloat());
    }

    private List<BBAnimation> loadAnimations(JsonArray animations) {
        List<BBAnimation> list = new ArrayList<>();
        for (JsonElement elem : animations) {
//            list.add(loadAnimation(elem.getAsJsonObject()));
        }
        return list;
    }

    private List<BBTexture> loadTextures(JsonArray textures) {
        List<BBTexture> list = new ArrayList<>();
        for (JsonElement elem : textures) {
            list.add(loadTexture(elem.getAsJsonObject()));
        }
        return list;
    }

    private BBTexture loadTexture(JsonObject textureJson) {
        String path = textureJson.getAsJsonPrimitive("path").getAsString();
        String name = textureJson.getAsJsonPrimitive("name").getAsString();
        String folder = textureJson.getAsJsonPrimitive("folder").getAsString();
        String namespace = textureJson.getAsJsonPrimitive("namespace").getAsString();
        String id = textureJson.getAsJsonPrimitive("id").getAsString();
        int width = textureJson.getAsJsonPrimitive("width").getAsInt();
        int height = textureJson.getAsJsonPrimitive("height").getAsInt();
        int uvWidth = textureJson.getAsJsonPrimitive("uv_width").getAsInt();
        int uvHeight = textureJson.getAsJsonPrimitive("uv_height").getAsInt();
        boolean particle = textureJson.getAsJsonPrimitive("particle").getAsBoolean();
        boolean layersEnabled = textureJson.getAsJsonPrimitive("layers_enabled").getAsBoolean();
        String syncToProject = textureJson.getAsJsonPrimitive("sync_to_project").getAsString();
        String renderMode = textureJson.getAsJsonPrimitive("render_mode").getAsString();
        String renderSides = textureJson.getAsJsonPrimitive("render_sides").getAsString();
        int frameTime = textureJson.getAsJsonPrimitive("frame_time").getAsInt();
        String frameOrderType = textureJson.getAsJsonPrimitive("frame_order_type").getAsString();
        String frameOrder = textureJson.getAsJsonPrimitive("frame_order").getAsString();
        boolean frameInterpolate = textureJson.getAsJsonPrimitive("frame_interpolate").getAsBoolean();

        boolean visible = textureJson.getAsJsonPrimitive("visible").getAsBoolean();
        boolean internal = textureJson.getAsJsonPrimitive("internal").getAsBoolean();
        boolean saved = textureJson.getAsJsonPrimitive("saved").getAsBoolean();

        UUID uuid = UUID.fromString(textureJson.getAsJsonPrimitive("uuid").getAsString());
        String relativePath = textureJson.getAsJsonPrimitive("relative_path").getAsString();

        URL source = null;
        try {
            source = new URL(textureJson.getAsJsonPrimitive("source").getAsString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Base64.Decoder decoder = Base64.getDecoder();
        return new BBTexture(path, name, folder, namespace, id, width, height, uvWidth, uvHeight, particle, layersEnabled, syncToProject, renderMode, renderSides, frameTime, frameOrderType, frameOrder, frameInterpolate, visible, internal, saved, uuid, relativePath, source);
    }

    private BBModelOutliner loadOutliner(JsonArray outliner) {
        List<UUID> uuids = new ArrayList<>();
        BBModelOutlinerData data = null;

        for (JsonElement elem : outliner) {
            if (!elem.isJsonObject()) {
                if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                    uuids.add(UUID.fromString(elem.getAsString()));
                }
                continue;
            }

            JsonObject elemObj = elem.getAsJsonObject();
            String name = elemObj.getAsJsonPrimitive("name").getAsString();
            Vec3f origin = loadVec3(elemObj.getAsJsonArray("origin"));
            Color color = loadColor(elemObj.getAsJsonPrimitive("color").getAsInt());
            UUID uuid = UUID.fromString(elemObj.getAsJsonPrimitive("uuid").getAsString());
            boolean export = elemObj.getAsJsonPrimitive("export").getAsBoolean();
            boolean mirrorUV = elemObj.getAsJsonPrimitive("mirror_uv").getAsBoolean();
            boolean isOpen = elemObj.getAsJsonPrimitive("isOpen").getAsBoolean();
            boolean visibility = elemObj.getAsJsonPrimitive("visibility").getAsBoolean();
            int autouv = elemObj.getAsJsonPrimitive("autouv").getAsInt();
            List<UUID> children = new ArrayList<>();

            for (JsonElement child : elemObj.getAsJsonArray("children")) {
                children.add(UUID.fromString(child.getAsString()));
            }

            data = new BBModelOutlinerData(name, origin, color, uuid, export, mirrorUV, isOpen, visibility, autouv, children);
        }

        if (data == null) {
            throw new IllegalArgumentException("Missing outliner data");
        }

        return uuids.isEmpty() ? new BBModelOutliner(data) : new BBModelOutliner(data, Collections.unmodifiableList(uuids));
    }

    private List<BBModelElement> loadElements(JsonArray elements) {
        List<BBModelElement> processed = new ArrayList<>();
        for (JsonElement elem : elements) {
            if (!elem.isJsonObject()) continue;
            JsonObject elemObj = elem.getAsJsonObject();
            String type = elemObj.getAsJsonPrimitive("type").getAsString();
            switch (type) {
                case "cube" -> processed.add(loadCubeElement(elemObj.getAsJsonObject()));
                case "mesh" -> processed.add(loadMeshElement(elemObj.getAsJsonObject()));
            }
        }

        if (processed.isEmpty()) {
            UltracraftClient.LOGGER.warn("BlockBench model {} has no elements", this.id);
        }

        return processed;
    }

    private BBMeshModelElement loadMeshElement(JsonObject meshJson) {
        String name = meshJson.getAsJsonPrimitive("name").getAsString();
        Color color = loadColor(meshJson.getAsJsonPrimitive("color").getAsInt());
        Vec3f origin = loadVec3(meshJson.getAsJsonArray("origin"));
        Vec3f rotation = loadVec3(meshJson.getAsJsonArray("rotation"));
        boolean export = meshJson.getAsJsonPrimitive("export").getAsBoolean();
        boolean visibility = meshJson.getAsJsonPrimitive("visibility").getAsBoolean();
        boolean locked = meshJson.getAsJsonPrimitive("locked").getAsBoolean();
        String renderOrder = meshJson.getAsJsonPrimitive("render_order").getAsString();
        boolean allowMirrorModeling = meshJson.getAsJsonPrimitive("allow_mirror_modeling").getAsBoolean();

        Map<String, BBModelVertex> vertices = loadVertices(meshJson.getAsJsonObject("vertices"));

        List<BBModelMeshFace> faces = loadMeshFaces(vertices, meshJson.getAsJsonObject("faces"));

        return new BBMeshModelElement(name, color, origin, rotation, export, visibility, locked, renderOrder, allowMirrorModeling, faces);
    }

    private List<BBModelMeshFace> loadMeshFaces(Map<String, BBModelVertex> vertices, JsonObject faces) {
        List<BBModelMeshFace> processed = new ArrayList<>();
        for (Map.Entry<String, JsonElement> elem : faces.entrySet()) {
            processed.add(this.loadMeshFace(vertices, elem.getValue().getAsJsonObject()));
        }

        return processed;
    }

    private BBModelMeshFace loadMeshFace(Map<String, BBModelVertex> verticesRef, JsonObject asJsonObject) {
        Map<String, Vec2f> uvs = new HashMap<>();
        JsonObject uvJson = asJsonObject.getAsJsonObject("uv");

        List<BBModelVertex> vertices = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : uvJson.entrySet()) {
            uvs.put(entry.getKey(), loadVec2(entry.getValue().getAsJsonArray()));
        }

        JsonArray verticesJson = asJsonObject.getAsJsonArray("vertices");
        for (JsonElement elem : verticesJson) {
            vertices.add(verticesRef.get(elem.getAsString()));
            if (!uvs.containsKey(elem.getAsString()))
                throw new IllegalArgumentException("Missing uv for vertex: " + elem.getAsString());
        }

        for (Map.Entry<String, Vec2f> entry : uvs.entrySet())
            if (!vertices.contains(verticesRef.get(entry.getKey())))
                throw new IllegalArgumentException("Missing vertex for uv: " + entry.getKey());

        return new BBModelMeshFace(Collections.unmodifiableMap(uvs), Collections.unmodifiableList(vertices), asJsonObject.get("texture").getAsInt());
    }

    private Map<String, BBModelVertex> loadVertices(JsonObject vertices) {
        Map<String, BBModelVertex> processed = new HashMap<>();
        for (Map.Entry<String, JsonElement> elem : vertices.entrySet()) {
            processed.put(elem.getKey(), this.loadVertex(elem.getValue().getAsJsonArray()));
        }

        return processed;
    }

    private BBModelVertex loadVertex(JsonArray coords) {
        return new BBModelVertex(loadVec3(coords));
    }

    private List<Vec3f> loadVertices(JsonArray vertices) {
        List<Vec3f> processed = new ArrayList<>();
        for (JsonElement elem : vertices) {
            processed.add(loadVec3(elem.getAsJsonArray()));
        }

        return processed;
    }

    private BBCubeModelElement loadCubeElement(JsonElement elem) {
        String name = elem.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
        boolean boxUv = elem.getAsJsonObject().getAsJsonPrimitive("box_uv").getAsBoolean();
        boolean rescale = elem.getAsJsonObject().getAsJsonPrimitive("rescale").getAsBoolean();
        boolean locked = elem.getAsJsonObject().getAsJsonPrimitive("locked").getAsBoolean();
        String renderOrder = elem.getAsJsonObject().getAsJsonPrimitive("render_order").getAsString();
        boolean allowMirrorModeling = elem.getAsJsonObject().getAsJsonPrimitive("allow_mirror_modeling").getAsBoolean();
        Vec3f from = loadVec3(elem.getAsJsonObject().getAsJsonArray("from"));
        Vec3f to = loadVec3(elem.getAsJsonObject().getAsJsonArray("to"));
        float autouv = elem.getAsJsonObject().getAsJsonPrimitive("autouv").getAsFloat();
        Color color = loadColor(elem.getAsJsonObject().getAsJsonPrimitive("color").getAsInt());
        Vec3f origin = loadVec3(elem.getAsJsonObject().getAsJsonArray("origin"));

        List<BBModelFace> faces = loadFaces(elem.getAsJsonObject().getAsJsonObject("faces"));

        UUID uuid = UUID.fromString(elem.getAsJsonObject().getAsJsonPrimitive("uuid").getAsString());

        return new BBCubeModelElement(name, boxUv, rescale, locked, renderOrder, allowMirrorModeling, from, to, autouv, color, origin, faces, uuid);
    }

    private List<BBModelFace> loadFaces(JsonObject faces) {
        List<BBModelFace> processed = new ArrayList<>();
        for (Map.Entry<String, JsonElement> face : faces.entrySet()) {
            processed.add(loadFace(face));
        }

        return processed;
    }

    private BBModelFace loadFace(Map.Entry<String, JsonElement> faceData) {
        String face = faceData.getKey();
        CubicDirection blockFace = CubicDirection.valueOf(face.toUpperCase(Locale.ROOT));

        JsonObject faceJson = faceData.getValue().getAsJsonObject();
        Vec4f uv = loadVec4(faceJson.getAsJsonArray("uv"));
        int texture = faceJson.get("texture").getAsInt();

        return new BBModelFace(blockFace, uv, texture);
    }

    private Color loadColor(int color) {
        return Color.BLACK;
    }

    private Vec2f loadVec2(JsonArray array) {
        return new Vec2f(array.get(0).getAsFloat(), array.get(1).getAsFloat());
    }

    private Vec3f loadVec3(JsonArray array) {
        return new Vec3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private Vec4f loadVec4(JsonArray array) {
        return new Vec4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
    }

    private BBMeta loadMeta(JsonObject jsonObject) {
        String formatVersion = jsonObject.getAsJsonPrimitive("format_version").getAsString();
        BBModelFormat modelFormat = BBModelFormat.valueOf(jsonObject.getAsJsonPrimitive("model_format").getAsString().toUpperCase(Locale.ROOT));
        boolean boxUv = jsonObject.getAsJsonPrimitive("box_uv").getAsBoolean();

        return new BBMeta(formatVersion, modelFormat, boxUv);
    }

    public BBMeta getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public Vec3f getVisibleBox() {
        return visibleBox;
    }

    public Vec2f getResolution() {
        return resolution;
    }

    public List<BBModelElement> getElements() {
        return elements;
    }

    public Identifier getId() {
        return id;
    }

    public BBModelOutliner getOutliner() {
        return outliner;
    }

    public List<BBAnimation> getAnimations() {
        return animations;
    }

    public Model getModel() {
        return model;
    }

    public Model createModel() {
        ModelBuilder model = new ModelBuilder();
        model.begin();
        Map<Integer, MeshBuilder> texture2builder = new HashMap<>();
        for (int i = 0; i < textures.size(); i++) {
            MeshBuilder value = new MeshBuilder();
            value.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
            System.out.println(textures.get(i).id());
            texture2builder.put(i, value);
        }
        for (BBModelElement element : elements) {
            writeElement(model, texture2builder, element);
        }

        for (Map.Entry<Integer, MeshBuilder> builder : texture2builder.entrySet()) {
            UltracraftClient.invokeAndWait(() -> {
                try {
                    System.out.println(textures.get(builder.getKey()).id());

                    return model.part(textures.get(builder.getKey()).id(), builder.getValue().end(), GL20.GL_TRIANGLES, Utils.make(new Material(), (mat) -> {
                        try {
                            mat.set(TextureAttribute.createDiffuse(textures.get(builder.getKey()).loadOrGetTexture()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                } catch (Exception e) {
                    throw new RuntimeException("Texture " + textures.get(builder.getKey()).id() + " failed to load", e);
                }

            });
        }

        return this.model = model.end();
    }

    private void writeElement(ModelBuilder model, Map<Integer, MeshBuilder> texture2builder, BBModelElement element) {
        element.write(model, texture2builder, this, resolution);
    }
}
