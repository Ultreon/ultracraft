package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.craft.client.model.model.Json5Model;
import com.ultreon.craft.client.model.model.Json5ModelLoader;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import com.ultreon.libs.commons.v0.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BlockModelRegistry {
    private static final Map<Block, List<Pair<Predicate<BlockMetadata>, Supplier<CubeModel>>>> REGISTRY = new HashMap<>();
    private static final Map<Block, List<Pair<Predicate<BlockMetadata>, Supplier<BlockModel>>>> CUSTOM_REGISTRY = new HashMap<>();
    private static final Set<Identifier> TEXTURES = new HashSet<>();
    private static final Map<Block, List<Pair<Predicate<BlockMetadata>, Supplier<BlockModel>>>> FINISHED_REGISTRY = new HashMap<>();

    static {
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking1"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking2"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking3"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking4"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking5"));
        BlockModelRegistry.TEXTURES.add(new Identifier("misc/breaking6"));
    }

    public static BlockModel get(BlockMetadata meta) {
        return BlockModelRegistry.CUSTOM_REGISTRY.getOrDefault(meta.getBlock(), new ArrayList<>())
                .stream()
                .filter(p -> p.getFirst().test(meta)).map(p -> p.getSecond().get())
                .findFirst()
                .orElse(null);
    }

    public static void register(Block block, Predicate<BlockMetadata> predicate, CubeModel model) {
        BlockModelRegistry.REGISTRY.computeIfAbsent(block, key -> new ArrayList<>()).add(new Pair<>(predicate, () -> model));
    }

    public static void registerCustom(Block block, Predicate<BlockMetadata> predicate, Supplier<BlockModel> model) {
        BlockModelRegistry.CUSTOM_REGISTRY.computeIfAbsent(block, key -> new ArrayList<>()).add(new Pair<>(predicate, Suppliers.memoize(model::get)));
    }

    public static void register(Supplier<Block> block, Predicate<BlockMetadata> predicate, Supplier<CubeModel> model) {
        BlockModelRegistry.REGISTRY.computeIfAbsent(block.get(), key -> new ArrayList<>()).add(new Pair<>(predicate, Suppliers.memoize(model::get)));
    }

    public static void registerDefault(Block block) {
        Identifier key = Registries.BLOCK.getId(block);
        Preconditions.checkNotNull(key, "Block is not registered");
        BlockModelRegistry.register(block, meta -> true, CubeModel.of(key.mapPath(path -> "blocks/" + path)));
    }

    public static void registerDefault(Supplier<Block> block) {
        BlockModelRegistry.register(block, meta -> true, Suppliers.memoize(() -> {
            Identifier key = Registries.BLOCK.getId(block.get());
            Preconditions.checkNotNull(key, "Block is not registered");
            return CubeModel.of(key.mapPath(path -> "blocks/" + path));
        }));
    }

    public static TextureAtlas stitch(TextureManager textureManager) {
        TextureStitcher stitcher = new TextureStitcher(UltracraftClient.id("block"));

        BlockModelRegistry.REGISTRY.values().stream().flatMap(Collection::stream).map(pair -> pair.getSecond().get().all()).forEach(BlockModelRegistry.TEXTURES::addAll);

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
        ImmutableMap.Builder<Block, List<Pair<Predicate<BlockMetadata>, BakedCubeModel>>> bakedModels = new ImmutableMap.Builder<>();
        BlockModelRegistry.REGISTRY.forEach((block, models) -> {
            List<Pair<Predicate<BlockMetadata>, BakedCubeModel>> modelList = new ArrayList<>();
            for (var modelPair : models) {
                var predicate = modelPair.getFirst();
                var model = modelPair.getSecond();
                BakedCubeModel baked = model.get().bake(block.getId(), atlas);

                modelList.add(new Pair<>(predicate, baked));
            }
            bakedModels.put(block, modelList);
        });

        return new BakedModelRegistry(atlas, bakedModels.build());
    }

    public static void bakeJsonModels(UltracraftClient client) {
        for (var entry : CUSTOM_REGISTRY.entrySet()) {
            List<Pair<Predicate<BlockMetadata>, Supplier<BlockModel>>> models = new ArrayList<>();
            for (var pair : entry.getValue()) {
                BlockModel model = pair.getSecond().get();
                UltracraftClient.invokeAndWait(() -> model.load(client));
                models.add(new Pair<>(pair.getFirst(), Suppliers.memoize(() -> model)));
            }
            FINISHED_REGISTRY.put(entry.getKey(), models);
        }
    }

    public static void load(Json5ModelLoader loader) {
        for (Block value : Registries.BLOCK.values()) {
            if (!REGISTRY.containsKey(value)) {
                try {
                    Json5Model load = loader.load(value);
                    if (load != null) {
                        CUSTOM_REGISTRY.computeIfAbsent(value, key -> new ArrayList<>()).add(new Pair<>(meta -> true, () -> load));

                        load.getOverrides().cellSet().forEach((cell) -> CUSTOM_REGISTRY.computeIfAbsent(value, key -> new ArrayList<>()).add(new Pair<>(meta -> meta.getEntryUnsafe(cell.getRowKey()).equals(cell.getColumnKey()), cell::getValue)));
                    } else if (value.doesRender()) {
                        BlockModelRegistry.registerDefault(value);
                    }
                } catch (IOException e) {
                    UltracraftClient.LOGGER.error("Failed to load block model for " + value.getId(), e);
                }
            }
        }

        for (var entry : CUSTOM_REGISTRY.entrySet()) {
            List<Pair<Predicate<BlockMetadata>, Supplier<BlockModel>>> models = new ArrayList<>();
            for (var pair : entry.getValue()) {
                BlockModel model = pair.getSecond().get();
                models.add(new Pair<>(pair.getFirst(), Suppliers.memoize(() -> model)));
            }
            FINISHED_REGISTRY.put(entry.getKey(), models);
        }
    }
}
