package com.ultreon.craft.client.shaders;

import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public interface ShaderProviderFactory<T extends ShaderProvider> {

    T create();
}
