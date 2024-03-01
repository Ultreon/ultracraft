package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.craft.client.model.JsonModel;
import com.ultreon.craft.client.model.JsonModelLoader;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class BlockModelRegistry {
    private static final Map<Block, Supplier<CubeModel>> REGISTRY = new HashMap<>();
    private static final Map<Block, Supplier<BlockModel>> CUSTOM_REGISTRY = new HashMap<>();
    private static final Set<Identifier> TEXTURES = new HashSet<>();
    private static final Map<Block, BlockModel> FINISHED_REGISTRY = new HashMap<>();

    static {
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking1"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking2"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking3"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking4"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking5"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking6"));
    }

    public static BlockModel get(Block block) {
        return BlockModelRegistry.CUSTOM_REGISTRY.getOrDefault(block, () -> null).get();
    }

    public static void register(Block block, CubeModel model) {
        BlockModelRegistry.REGISTRY.put(block, () -> model);
    }

    public static void registerCustom(Block block, Supplier<BlockModel> model) {
        BlockModelRegistry.CUSTOM_REGISTRY.put(block, Suppliers.memoize(model::get));
    }

    public static void register(Supplier<Block> block, Supplier<CubeModel> model) {
        BlockModelRegistry.REGISTRY.put(block.get(), model);
    }

    public static void registerDefault(Block block) {
        Identifier key = Registries.BLOCK.getId(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        BlockModelRegistry.register(block, CubeModel.of(key.mapPath(path -> "blocks/" + path)));
    }

    public static void registerDefault(Supplier<Block> block) {
        BlockModelRegistry.register(block, Suppliers.memoize(() -> {
            Identifier key = Registries.BLOCK.getId(block.get());
            Preconditions.checkNotNull(key, "Block is not registered");
            return CubeModel.of(key.mapPath(path -> "blocks/" + path));
        }));
    }

    public static TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher stitcher = new TextureStitcher(UltracraftClient.id("block"));

        for (Supplier<CubeModel> value : BlockModelRegistry.REGISTRY.values()) {
            BlockModelRegistry.TEXTURES.addAll(value.get().all());
        }

        final int breakStages = 6;

        for (int i = 0; i < breakStages; i++) {
            Identifier texId = new Identifier("textures/misc/breaking" + (i + 1) + ".png");
            Texture tex = textureManager.getTexture(texId);
            stitcher.add(texId, tex);
        }

        for (Identifier texture : BlockModelRegistry.TEXTURES) {
            Texture emissive = textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".emissive.png"), null);
            if (emissive != null) {
                stitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")), emissive);
            } else {
                stitcher.add(texture, textureManager.getTexture(texture.mapPath(path -> "textures/" + path + ".png")));
            }
        }

        return stitcher.stitch();
    }

    public static BakedModelRegistry bake(TextureAtlas atlas) {
        ImmutableMap.Builder<Block, BakedCubeModel> bakedModels = new ImmutableMap.Builder<>();
        BlockModelRegistry.REGISTRY.forEach((block, model) -> bakedModels.put(block, model.get().bake(block.getId(), atlas)));

        return new BakedModelRegistry(atlas, bakedModels.build());
    }

    public static void bakeJsonModels(UltracraftClient client) {
        for (var entry : CUSTOM_REGISTRY.entrySet()) {
            BlockModel model = entry.getValue().get();
            UltracraftClient.invokeAndWait(() -> model.load(client));
            FINISHED_REGISTRY.put(entry.getKey(), model);
        }
    }

    public static void load(JsonModelLoader loader) {
        for (Block value : Registries.BLOCK.getValues()) {
            if (!REGISTRY.containsKey(value)) {
                try {
                    JsonModel load = loader.load(value);
                    if (load != null) {
                        CUSTOM_REGISTRY.put(value, () -> load);
                    } else if (value.doesRender()) {
                        BlockModelRegistry.registerDefault(value);
                    }
                } catch (IOException e) {
                    UltracraftClient.LOGGER.error("Failed to load block model for " + value.getId(), e);
                }
            }
        }
    }
}
