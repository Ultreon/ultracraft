package com.ultreon.craft.client.resources;

import com.ultreon.craft.util.ElementID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(ElementID id) {
        super(id.toString());
    }
}
