package com.ultreon.craft;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.BakedModelRegistry;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.resources.ResourceFileHandle;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockModelRegistry {
    private static final Map<Block, CubeModel> REGISTRY = new HashMap<>();
    private static final Set<Identifier> TEXTURES = new HashSet<>();

    public static void register(Block block, CubeModel model) {
        REGISTRY.put(block, model);
        TEXTURES.addAll(model.all());
    }

    public static TextureAtlas stitch() {
        PixmapPacker packer = new PixmapPacker(512, 512, Pixmap.Format.RGBA8888, 0, false);

        for (Identifier texture : TEXTURES) {
            packer.pack(texture.toString(), new Pixmap(new ResourceFileHandle(texture.mapPath(path -> "textures/" + path))));
        }

        return packer.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, true);
    }

    public static BakedModelRegistry bake(TextureAtlas atlas) {
        var bakedModels = new ImmutableMap.Builder<Block, BakedCubeModel>();
        REGISTRY.forEach((block, model) -> bakedModels.put(block, model.bake(atlas)));

        return new BakedModelRegistry(atlas, bakedModels.build());
    }
}
