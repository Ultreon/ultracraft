package com.ultreon.craft.client.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.ModelType;
import com.ultreon.craft.resources.Resource;
import com.ultreon.craft.util.ElementID;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import static org.jetbrains.annotations.ApiStatus.Experimental;
import static org.jetbrains.annotations.ApiStatus.Internal;

public class ResourceLoader {
    private static final GLTFLoader gltfLoader = new GLTFLoader();
    private static final GLBLoader glbLoader = new GLBLoader();
    private static final G3dModelLoader g3djLoader = new G3dModelLoader(new JsonReader());
    private static final G3dModelLoader g3dbLoader = new G3dModelLoader(new UBJsonReader());

    /**
     * Loads a model from a static resource.
     *
     * @param resource the static resource to load a model from.
     * @return the loaded model.
     */
    public static SceneAsset loadGLTF(ElementID resource) {
        FileHandle file = UltracraftClient.resource(resource.mapPath(path -> "models/" + path));

        if (resource.path().endsWith(".glb")) return glbLoader.load(file, true);
        else if (resource.path().endsWith(".gltf")) return gltfLoader.load(file, true);
        throw new GdxRuntimeException("Unsupported GLTF model type: " + file.extension());
    }

    /**
     * Loads a model from a dynamic resource.
     * <p><b>WARNING: THIS METHOD IS EXPERIMENTAL, AND CAN LEAD TO UNEXPECTED BEHAVIOR.</b></p>
     *
     * @param resource the dynamic resource to load a model from.
     * @param type the model type.
     * @return the loaded model.
     */
    @Experimental
    public static SceneAsset loadGLTF(Resource resource, ModelType type) {
        if (type == ModelType.GLB) return glbLoader.load(new ResourceFileHandle(resource));
        if (type == ModelType.GLTF) return gltfLoader.load(new ResourceFileHandle(resource));
        throw new GdxRuntimeException("Unsupported GLTF model type: " + type.name());
    }

    /**
     * Loads a model from a static resource.
     *
     * @param resource the static resource to load a model from.
     * @return the loaded model.
     */
    public static Model loadG3D(ElementID resource) {
        FileHandle file = UltracraftClient.resource(resource.mapPath(path -> "models/" + path));

        TextureProvider textureProvider = fileName -> {
            String s = "assets/" + resource.namespace() + "/models";
            if (fileName.startsWith(s)) {
                String filePath = fileName.substring(s.length() + 1);
                return new Texture(UltracraftClient.resource(resource.mapPath(path -> "textures/" + filePath)));
            }
            return new Texture(UltracraftClient.resource(resource.withPath("textures/" + fileName)).path());
        };

        if (resource.path().endsWith(".g3dj")) return g3djLoader.loadModel(file, textureProvider);
        else if (resource.path().endsWith(".g3db")) return g3dbLoader.loadModel(file, textureProvider);
        throw new GdxRuntimeException("Unsupported G3D model type: " + file.extension());
    }

    /**
     * Loads a model from a dynamic resource.
     * <p><b>WARNING: THIS METHOD IS EXPERIMENTAL, AND CAN LEAD TO UNEXPECTED BEHAVIOR.</b></p>
     *
     * @param resource the dynamic resource to load a model from.
     * @param type     the model type.
     * @return the loaded model.
     */
    @Experimental
    public static Model loadG3D(Resource resource, ModelType type, TextureProvider textureProvider) {
        if (type == ModelType.G3DJ) return g3djLoader.loadModel(new ResourceFileHandle(resource), textureProvider);
        if (type == ModelType.G3DB) return g3dbLoader.loadModel(new ResourceFileHandle(resource), textureProvider);
        throw new GdxRuntimeException("Unsupported G3D model type: " + type.name());
    }

    /**
     * Initializes the resource loader utility class.
     * @param client the client instance.
     */
    @Internal
    public static void init(UltracraftClient client) {
        client.deferDispose(gltfLoader);
        client.deferDispose(glbLoader);
    }
}
