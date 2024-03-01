package com.ultreon.craft.client.resources;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.util.Identifier;

public interface LoadableResource {
    void load(UltracraftClient client);

    Identifier resourceId();

}
