package com.ultreon.craft.client.resources;

import com.ultreon.craft.util.Identifier;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Identifier id) {
        super(id.toString());
    }
}
