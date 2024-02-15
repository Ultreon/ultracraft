package com.ultreon.craft.client.registry;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.client.InternalApi;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class BlockEntityModelRegistry {
    private static final Map<BlockEntityType<?>, Function<ElementID, BlockModel>> REGISTRY = new HashMap<>();
    private static final Map<BlockEntityType<?>, BlockModel> FINISHED_REGISTRY = new HashMap<>();

    private BlockEntityModelRegistry() {

    }

    public static <T extends BlockEntity> void register(BlockEntityType<T> type, Function<ElementID, BlockModel> modelFactory) {
        REGISTRY.put(type, modelFactory);
    }

    @InternalApi
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
