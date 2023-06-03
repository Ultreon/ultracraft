package com.ultreon.craft;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.BakedModelRegistry;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;
import com.ultreon.craft.render.texture.atlas.TextureStitcher;
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

    public static void registerDefault(Block block) {
        Identifier key = Registries.BLOCK.getKey(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        register(block, CubeModel.of(key.mapPath(path -> "blocks/" + path)));
    }

    public static TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher textureStitcher = new TextureStitcher();

        textureStitcher.add(new Identifier("missingno"), TextureManager.DEFAULT_TEXTURE);

        for (Identifier texture : TEXTURES) {
            textureStitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")));
        }

        return textureStitcher.stitch();
    }

    public static BakedModelRegistry bake(TextureAtlas atlas) {
        var bakedModels = new ImmutableMap.Builder<Block, BakedCubeModel>();
        REGISTRY.forEach((block, model) -> bakedModels.put(block, model.bake(atlas)));

        return new BakedModelRegistry(atlas, bakedModels.build());
    }
}
