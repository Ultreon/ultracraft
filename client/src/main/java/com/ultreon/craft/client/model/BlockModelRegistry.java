package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class BlockModelRegistry {
    private static final Map<Supplier<Block>, Supplier<CubeModel>> REGISTRY = new HashMap<>();
    private static final Set<Identifier> TEXTURES = new HashSet<>();

    static {
        TEXTURES.add(new Identifier("misc/breaking1"));
        TEXTURES.add(new Identifier("misc/breaking2"));
        TEXTURES.add(new Identifier("misc/breaking3"));
        TEXTURES.add(new Identifier("misc/breaking4"));
        TEXTURES.add(new Identifier("misc/breaking5"));
        TEXTURES.add(new Identifier("misc/breaking6"));
    }

    public static void register(Block block, CubeModel model) {
        REGISTRY.put(() -> block, () -> model);
    }

    public static void register(Supplier<Block> block, Supplier<CubeModel> model) {
        REGISTRY.put(block, model);
    }

    public static void registerDefault(Block block) {
        Identifier key = Registries.BLOCKS.getKey(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        register(block, CubeModel.of(key.mapPath(path -> "blocks/" + path)));
    }

    public static void registerDefault(Supplier<Block> block) {
        register(block, Suppliers.memoize(() -> {
            Identifier key = Registries.BLOCKS.getKey(block.get());
            Preconditions.checkNotNull(key, "Block is not registered");
            return CubeModel.of(key.mapPath(path -> "blocks/" + path));
        }));
    }

    public static TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher textureStitcher = new TextureStitcher();

        for (Supplier<CubeModel> value : REGISTRY.values()) {
            TEXTURES.addAll(value.get().all());
        }

        final int breakStages = 6;

        for (int i = 0; i < breakStages; i++) {
            Identifier texId = new Identifier("textures/misc/breaking" + (i + 1) + ".png");
            Texture tex = textureManager.getTexture(texId);
            textureStitcher.add(texId, tex);
        }

        for (Identifier texture : TEXTURES) {
            textureStitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")));
        }

        return textureStitcher.stitch();
    }

    public static BakedModelRegistry bake(TextureAtlas atlas) {
        ImmutableMap.Builder<Block, BakedCubeModel> bakedModels = new ImmutableMap.Builder<>();
        REGISTRY.forEach((block, model) -> bakedModels.put(block.get(), model.get().bake(atlas)));

        return new BakedModelRegistry(atlas, bakedModels.build());
    }
}
