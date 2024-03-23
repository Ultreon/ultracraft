package com.ultreon.craft.client.management;

import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.base.Supplier;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.resources.ReloadContext;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShaderProviderManager implements Manager<ShaderProvider> {
    private final Map<Identifier, ShaderProvider> shaders = new LinkedHashMap<>();
    private final LinkedHashMap<Identifier, Supplier<? extends ShaderProvider>> shaderProviderFactories = new LinkedHashMap<>();

    @Override
    public ShaderProvider register(@NotNull Identifier id, @NotNull ShaderProvider shaderProvider) {
        this.shaders.put(id, shaderProvider);
        return shaderProvider;
    }
    
    public <T extends ShaderProvider> Supplier<T> register(@NotNull Identifier id, @NotNull Supplier<T> factory) {
        Supplier<T> memoize = create(id, factory);
        this.shaderProviderFactories.put(id, memoize);
        return memoize;
    }

    @SafeVarargs
    private <T extends ShaderProvider> Supplier<T> create(Identifier id, Supplier<T> create, T... typeGetter) {
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) typeGetter.getClass().getComponentType();
        return () -> {
            if (this.shaders.containsKey(id)) {
                return clazz.cast(this.shaders.get(id));
            }

            return UltracraftClient.invokeAndWait(() -> {
                T provider = create.get();
                this.shaders.put(id, provider);
                return provider;
            });
        };
    }

    @Override
    public @Nullable ShaderProvider get(Identifier id) {
        ShaderProvider shaderProvider = this.shaders.get(id);

        if (shaderProvider == null) {
            throw new GdxRuntimeException("Shader provider not found: " + id);
        }

        return shaderProvider;
    }

    @Override
    public void reload(ReloadContext context) {
        for (ShaderProvider shaderProvider : List.copyOf(this.shaders.values())) {
            context.submit(shaderProvider::dispose);
        }

        this.shaders.clear();

        this.shaderProviderFactories.forEach((id, factory) -> {
            ShaderProvider provider = factory.get();
            this.shaders.put(id, provider);
            context.submit(provider::dispose);
        });
    }
}
