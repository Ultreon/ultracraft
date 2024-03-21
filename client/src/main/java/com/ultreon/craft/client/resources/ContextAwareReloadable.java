package com.ultreon.craft.client.resources;

import com.ultreon.craft.client.resources.ReloadContext;
import com.ultreon.craft.resources.ResourceManager;

public interface ContextAwareReloadable {
    void reload(ResourceManager resourceManager, ReloadContext context);
}
