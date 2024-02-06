package com.ultreon.craft.client.resources;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.util.ElementID;

public interface LoadableResource {
    void load(UltracraftClient client);

    ElementID resourceId();

}
