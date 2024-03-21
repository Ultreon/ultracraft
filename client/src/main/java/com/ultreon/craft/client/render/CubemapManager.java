package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.Cubemap;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.client.resources.ReloadContext;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Identifier;
import de.marhali.json5.Json5Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class CubemapManager {
    private final Map<Identifier, Cubemap> cubemaps = new LinkedHashMap<>();
    private final ResourceManager resourceManager;

    public CubemapManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void registerCubemap(Identifier id, Cubemap cubemap) {
        this.cubemaps.put(id, cubemap);
    }

    public void loadCubemap(Identifier id) {
        try (InputStream inputStream = resourceManager.openResourceStream(id)) {
            Json5Object root = CommonConstants.JSON5.parse(inputStream).getAsJson5Object();

            String identifier = root.getAsJson5Primitive("target_pos_x").getAsString();
            Identifier targetPosX = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_neg_x").getAsString();
            Identifier targetNegX = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_pos_y").getAsString();
            Identifier targetPosY = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_neg_y").getAsString();
            Identifier targetNegY = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_pos_z").getAsString();
            Identifier targetPosZ = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_neg_z").getAsString();
            Identifier targetNegZ = new Identifier(identifier);

            Cubemap cubemap = new Cubemap(
                    new ResourceFileHandle(targetPosX.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetNegX.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetPosY.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetNegY.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetPosZ.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetNegZ.mapPath(p -> "textures/cubemap/" + p + ".png"))
            );

            this.registerCubemap(id, cubemap);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load cubemap {}", id, e);
        }
    }

    public Cubemap getCubemap(Identifier id) {
        return this.cubemaps.get(id);
    }

    public void reload(ReloadContext context) {
        for (Cubemap cubemap : Map.copyOf(this.cubemaps).values()) {
            context.submit(cubemap::dispose);
        }

        this.cubemaps.clear();
    }

    public void dispose() {
        for (Cubemap cubemap : this.cubemaps.values()) {
            cubemap.dispose();
        }

        this.cubemaps.clear();
    }
}
