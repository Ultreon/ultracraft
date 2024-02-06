package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.RegistryKey;
import com.ultreon.craft.registry.RegistryKeys;
import com.ultreon.craft.resources.Resource;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Axis;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static com.ultreon.craft.client.UltracraftClient.GSON;

public class JsonModelLoader {
    private final ResourceManager resourceManager;

    public JsonModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public JsonModel load(Block block) throws IOException {
        ElementID elementID = block.getId().mapPath(path -> "models/blocks/" + path + ".json");
        Resource resource = this.resourceManager.getResource(elementID);
        if (resource == null)
            return null;
        UltracraftClient.LOGGER.debug("Loading block model: {}", elementID);
        return this.load(Registries.BLOCK.getKey(block), GSON.fromJson(resource.openReader(), JsonObject.class));
    }

    public JsonModel load(Item item) throws IOException {
        ElementID elementID = item.getId().mapPath(path -> "models/items/" + path + ".json");
        Resource resource = this.resourceManager.getResource(elementID);
        if (resource == null)
            return null;
        UltracraftClient.LOGGER.debug("Loading item model: {}", elementID);
        return this.load(Registries.ITEM.getKey(item), GSON.fromJson(resource.openReader(), JsonObject.class));
    }

    @SuppressWarnings("SpellCheckingInspection")
    public JsonModel load(RegistryKey<?> key, JsonElement modelData) {
        if (!key.parent().equals(RegistryKeys.BLOCK) && !key.parent().equals(RegistryKeys.ITEM)) {
            throw new IllegalArgumentException("Invalid model key, must be block or item: " + key);
        }

        JsonObject root = modelData.getAsJsonObject();
        JsonObject textures = root.getAsJsonObject("textures");
        Map<String, ElementID> textureElements = loadTextures(textures);

//        GridPoint2 textureSize = loadVec2i(root.getAsJsonArray("texture_size"), new GridPoint2(16, 16));
        GridPoint2 textureSize = new GridPoint2(16, 16);

        JsonArray elements = root.getAsJsonArray("elements");
        List<ModelElement> modelElements = loadElements(elements, textureSize.x, textureSize.y);

        JsonElement ambientocclusion = root.get("ambientocclusion");
        boolean ambientOcclusion = ambientocclusion == null || ambientocclusion.getAsBoolean();

        // TODO: Allow display properties.
        Display display = new Display();

        return new JsonModel(key, textureElements, modelElements, ambientOcclusion, display);
    }

    private GridPoint2 loadVec2i(JsonArray textureSize, GridPoint2 defaultValue) {
        if (textureSize == null)
            return defaultValue;
        if (textureSize.size() != 2)
            throw new IllegalArgumentException("Invalid 'texture_size' array: " + textureSize);
        return new GridPoint2(textureSize.get(0).getAsInt(), textureSize.get(1).getAsInt());
    }

    private List<ModelElement> loadElements(JsonArray elements, int textureWidth, int textureHeight) {
        List<ModelElement> modelElements = new ArrayList<>();

        for (JsonElement elem : elements) {
            JsonObject element = elem.getAsJsonObject();
            JsonObject faces = element.getAsJsonObject("faces");
            Map<BlockFace, FaceElement> blockFaceFaceElementMap = loadFaces(faces, textureWidth, textureHeight);

            JsonElement shade1 = element.get("shade");
            boolean shade = shade1 != null && shade1.getAsBoolean();
            JsonElement rotation1 = element.get("rotation");
            ElementRotation rotation = ElementRotation.deserialize(rotation1 == null ? null : rotation1.getAsJsonObject());

            Vector3 from = loadVec3(element.getAsJsonArray("from"));
            Vector3 to = loadVec3(element.getAsJsonArray("to"));

            ModelElement modelElement = new ModelElement(blockFaceFaceElementMap, shade, rotation, from, to);
            modelElements.add(modelElement);
        }

        return modelElements;
    }

    private Vector3 loadVec3(JsonArray from) {
        return new Vector3(from.get(0).getAsFloat(), from.get(1).getAsFloat(), from.get(2).getAsFloat());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private Map<BlockFace, FaceElement> loadFaces(JsonObject faces, int textureWidth, int textureHeight) {
        Map<BlockFace, FaceElement> faceElems = new HashMap<>();
        for (Map.Entry<String, JsonElement> face : faces.entrySet()) {
            BlockFace blockFace = BlockFace.valueOf(face.getKey().toUpperCase(Locale.ROOT));
            JsonObject faceData = face.getValue().getAsJsonObject();
            JsonArray uvs = faceData.getAsJsonArray("uv");
            String texture = faceData.get("texture").getAsString();
            JsonElement rotation1 = faceData.get("rotation");
            int rotation = rotation1 == null ? 0 : rotation1.getAsInt();
            JsonElement tintIndex1 = faceData.get("tintindex");
            int tintIndex = tintIndex1 == null ? -1 : tintIndex1.getAsInt();
            JsonElement cullface = faceData.get("cullface");
            String cullFace = cullface == null ? null : cullface.getAsString();

            faceElems.put(blockFace, new FaceElement(texture, new UVs(uvs.get(0).getAsInt(), uvs.get(1).getAsInt(), uvs.get(2).getAsInt(), uvs.get(3).getAsInt(), textureWidth, textureHeight), rotation, tintIndex, cullFace));
        }

        return faceElems;
    }

    private Map<String, ElementID> loadTextures(JsonObject textures) {
        Map<String, ElementID> textureElements = new HashMap<>();

        for (var entry : textures.entrySet()) {
            String name = entry.getKey();
            String stringId = entry.getValue().getAsString();
            ElementID id = ElementID.parse(stringId).mapPath(path -> "textures/" + path + ".png");
            textureElements.put(name, id);
        }

        return textureElements;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public record FaceElement(String texture, UVs uvs, int rotation, int tintindex,
                              String cullface) {
    }

    public static final class UVs {
        private final float x1;
        private final float y1;
        private final float x2;
        private final float y2;

        public UVs(float x1, float y1, float x2, float y2) {
            this.x1 = x1 / 16.0F;
            this.y1 = y1 / 16.0F;
            this.x2 = x2 / 16.0F;
            this.y2 = y2 / 16.0F;
        }

        public UVs(float x1, float y1, float x2, float y2, int textureWidth, int textureHeight) {
            this.x1 = x1 / textureWidth;
            this.y1 = y1 / textureHeight;
            this.x2 = x2 / textureWidth;
            this.y2 = y2 / textureHeight;
        }

        public float x1() {
            return x1;
        }

        public float y1() {
            return y1;
        }

        public float x2() {
            return x2;
        }

        public float y2() {
            return y2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (UVs) obj;
            return Float.floatToIntBits(this.x1) == Float.floatToIntBits(that.x1) &&
                    Float.floatToIntBits(this.y1) == Float.floatToIntBits(that.y1) &&
                    Float.floatToIntBits(this.x2) == Float.floatToIntBits(that.x2) &&
                    Float.floatToIntBits(this.y2) == Float.floatToIntBits(that.y2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x1, y1, x2, y2);
        }

        @Override
        public String toString() {
            return "UVs[" +
                    "x1=" + x1 + ", " +
                    "y1=" + y1 + ", " +
                    "x2=" + x2 + ", " +
                    "y2=" + y2 + ']';
        }


    }
    public record ModelElement(Map<BlockFace, FaceElement> blockFaceFaceElementMap, boolean shade,
                               ElementRotation rotation, Vector3 from, Vector3 to) {
        private static final Vector3 tmp = new Vector3();
        private static final Quaternion tmpQ = new Quaternion();

        public ModelElement {
            Preconditions.checkNotNull(blockFaceFaceElementMap);
            Preconditions.checkNotNull(rotation);
            Preconditions.checkNotNull(from);
            Preconditions.checkNotNull(to);
        }

        public void bake(int idx, ModelBuilder modelBuilder, Map<String, ElementID> textureElements) {
            Vector3 from = this.from();
            Vector3 to = this.to();

            ModelBuilder nodeBuilder = new ModelBuilder();
            nodeBuilder.begin();

            MeshBuilder meshBuilder = new MeshBuilder();
            VertexInfo v00 = new VertexInfo();
            VertexInfo v01 = new VertexInfo();
            VertexInfo v10 = new VertexInfo();
            VertexInfo v11 = new VertexInfo();
            for (Map.Entry<BlockFace, FaceElement> entry : blockFaceFaceElementMap.entrySet()) {
                BlockFace blockFace = entry.getKey();
                FaceElement faceElement = entry.getValue();
                String texRef = faceElement.texture;
                ElementID texture;

                if (texRef.equals("#missing")) texture = new ElementID("textures/block/error.png");
                else if (texRef.startsWith("#")) texture = textureElements.get(texRef.substring(1));
                else texture = ElementID.parse(texRef).mapPath(path -> "textures/" + path + ".png");

                meshBuilder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)), GL20.GL_TRIANGLES);

                v00.setNor(blockFace.getNormal());
                v01.setNor(blockFace.getNormal());
                v10.setNor(blockFace.getNormal());
                v11.setNor(blockFace.getNormal());

                v00.setUV(faceElement.uvs.x1, faceElement.uvs.y2);
                v01.setUV(faceElement.uvs.x1, faceElement.uvs.y1);
                v10.setUV(faceElement.uvs.x2, faceElement.uvs.y2);
                v11.setUV(faceElement.uvs.x2, faceElement.uvs.y1);

                switch (blockFace) {
                    case UP -> {
                        v00.setPos(to.x, to.y, from.z);
                        v01.setPos(to.x, to.y, to.z);
                        v10.setPos(from.x, to.y, from.z);
                        v11.setPos(from.x, to.y, to.z);
                    }
                    case DOWN -> {
                        v00.setPos(from.x, from.y, from.z);
                        v01.setPos(from.x, from.y, to.z);
                        v10.setPos(to.x, from.y, from.z);
                        v11.setPos(to.x, from.y, to.z);
                    }
                    case WEST -> {
                        v00.setPos(from.x, from.y, from.z);
                        v01.setPos(from.x, to.y, from.z);
                        v10.setPos(from.x, from.y, to.z);
                        v11.setPos(from.x, to.y, to.z);
                    }
                    case EAST -> {
                        v00.setPos(to.x, from.y, to.z);
                        v01.setPos(to.x, to.y, to.z);
                        v10.setPos(to.x, from.y, from.z);
                        v11.setPos(to.x, to.y, from.z);
                    }
                    case NORTH -> {
                        v00.setPos(to.x, from.y, from.z);
                        v01.setPos(to.x, to.y, from.z);
                        v10.setPos(from.x, from.y, from.z);
                        v11.setPos(from.x, to.y, from.z);
                    }
                    case SOUTH -> {
                        v00.setPos(from.x, from.y, to.z);
                        v01.setPos(from.x, to.y, to.z);
                        v10.setPos(to.x, from.y, to.z);
                        v11.setPos(to.x, to.y, to.z);
                    }
                }

                meshBuilder.rect(v00, v10, v11, v01);

                Material material = new Material();
                material.set(TextureAttribute.createDiffuse(UltracraftClient.get().getTextureManager().getTexture(texture)));
                material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                material.set(new FloatAttribute(FloatAttribute.AlphaTest));
                material.set(new DepthTestAttribute(GL20.GL_LEQUAL));
                nodeBuilder.part(idx + "." + blockFace.name(), meshBuilder.end(), GL20.GL_TRIANGLES, material);
            }

            Model end = nodeBuilder.end();
            Node node = modelBuilder.node("[" + idx + "]", end);

            Vector3 originVec = rotation.originVec;
            Axis axis = rotation.axis;
            float angle = rotation.angle;
            boolean rescale = rotation.rescale; // TODO: implement

            node.localTransform.translate(originVec.x, originVec.y, originVec.z);
            node.localTransform.rotate(axis.getVector(), angle);
            node.localTransform.translate(-originVec.x, -originVec.y, -originVec.z);
            node.scale.set(node.localTransform.getScale(tmp));
            node.translation.set(node.localTransform.getTranslation(tmp));
            node.rotation.set(node.localTransform.getRotation(tmpQ));
        }
    }

    public record ElementRotation(Vector3 originVec, Axis axis, float angle, boolean rescale) {

        public static ElementRotation deserialize(@Nullable JsonObject rotation) {
            if (rotation == null) {
                return new ElementRotation(new Vector3(0, 0, 0), Axis.Y, 0, false);
            }

            JsonArray origin = rotation.getAsJsonArray("origin");
            String axis = rotation.get("axis").getAsString();
            float angle = rotation.get("angle").getAsFloat();
            JsonElement rescale1 = rotation.get("rescale");
            boolean rescale = rescale1 != null && rescale1.getAsBoolean();

            Vector3 originVec = new Vector3(origin.get(0).getAsFloat(), origin.get(1).getAsFloat(), origin.get(2).getAsFloat());
            return new ElementRotation(originVec, Axis.valueOf(axis.toUpperCase(Locale.ROOT)), angle, rescale);
        }
    }

    public record Display() {

    }
}
