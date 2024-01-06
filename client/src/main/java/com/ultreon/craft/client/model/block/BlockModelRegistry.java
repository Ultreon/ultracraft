package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class BlockModelRegistry {
    private static final Map<Supplier<Block>, Supplier<CubeModel>> REGISTRY = new HashMap<>();
    private static final Set<ElementID> TEXTURES = new HashSet<>();

    static {
        BlockModelRegistry.TEXTURES.add(new ElementID("misc/breaking1"));
        BlockModelRegistry.TEXTURES.add(new ElementID("misc/breaking2"));
        BlockModelRegistry.TEXTURES.add(new ElementID("misc/breaking3"));
        BlockModelRegistry.TEXTURES.add(new ElementID("misc/breaking4"));
        BlockModelRegistry.TEXTURES.add(new ElementID("misc/breaking5"));
        BlockModelRegistry.TEXTURES.add(new ElementID("misc/breaking6"));
    }

    public static void register(Block block, CubeModel model) {
        BlockModelRegistry.REGISTRY.put(() -> block, () -> model);
    }

    public static void register(Supplier<Block> block, Supplier<CubeModel> model) {
        BlockModelRegistry.REGISTRY.put(block, model);
    }

    public static void registerDefault(Block block) {
        ElementID key = Registries.BLOCK.getKey(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        BlockModelRegistry.register(block, CubeModel.of(key.mapPath(path -> "blocks/" + path)));
    }

    public static void registerDefault(Supplier<Block> block) {
        BlockModelRegistry.register(block, Suppliers.memoize(() -> {
            ElementID key = Registries.BLOCK.getKey(block.get());
            Preconditions.checkNotNull(key, "Block is not registered");
            return CubeModel.of(key.mapPath(path -> "blocks/" + path));
        }));
    }

    public static TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher sitcher = new TextureStitcher(UltracraftClient.id("block"));

        for (Supplier<CubeModel> value : BlockModelRegistry.REGISTRY.values()) {
            BlockModelRegistry.TEXTURES.addAll(value.get().all());
        }

        final int breakStages = 6;

        for (int i = 0; i < breakStages; i++) {
            ElementID texId = new ElementID("textures/misc/breaking" + (i + 1) + ".png");
            Texture tex = textureManager.getTexture(texId);
            sitcher.add(texId, tex);
        }

        for (ElementID texture : BlockModelRegistry.TEXTURES) {
            Texture emissive = textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".emissive.png"), null);
            if (emissive != null) {
                sitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")), emissive);
            } else {
                sitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")));
            }
        }

        return sitcher.stitch();
    }

    public static BakedModelRegistry bake(TextureAtlas atlas) {
        ImmutableMap.Builder<Block, BakedCubeModel> bakedModels = new ImmutableMap.Builder<>();
        BlockModelRegistry.REGISTRY.forEach((block, model) -> bakedModels.put(block.get(), model.get().bake(atlas)));

        return new BakedModelRegistry(atlas, bakedModels.build());
    }
}
