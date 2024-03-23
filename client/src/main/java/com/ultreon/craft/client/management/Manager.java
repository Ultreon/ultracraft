package com.ultreon.craft.client.management;

import com.ultreon.craft.client.resources.ContextAwareReloadable;
import com.ultreon.craft.resources.ReloadContext;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Manager<T> extends ContextAwareReloadable {
    T register(@NotNull Identifier id, @NotNull T object);

    @Nullable T get(Identifier id);

    void reload(ReloadContext context);

    @Override
    default void reload(ResourceManager resourceManager, ReloadContext context) {
        this.reload(context);
    }
}
