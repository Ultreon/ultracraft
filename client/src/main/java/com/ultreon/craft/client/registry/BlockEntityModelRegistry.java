package com.ultreon.craft.client.registry;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class BlockEntityModelRegistry {
    private static final Map<BlockEntityType<?>, Function<Identifier, BlockModel>> REGISTRY = new HashMap<>();
    private static final Map<BlockEntityType<?>, BlockModel> FINISHED_REGISTRY = new HashMap<>();

    private BlockEntityModelRegistry() {

    }

    public static <T extends BlockEntity> void register(BlockEntityType<T> type, Function<Identifier, BlockModel> modelFactory) {
        REGISTRY.put(type, modelFactory);
    }

    public static void load(UltracraftClient client) {
        for (var entry : REGISTRY.entrySet()) {
            BlockModel model = entry.getValue().apply(Objects.requireNonNull(entry.getKey().getId()).mapPath(path -> "blocks/" + path + ".g3dj"));
            UltracraftClient.invokeAndWait(() -> model.load(client));
            FINISHED_REGISTRY.put(entry.getKey(), model);
        }
    }

    @Nullable
    public static BlockModel get(BlockEntityType<?> type) {
        return FINISHED_REGISTRY.get(type);
    }
}
