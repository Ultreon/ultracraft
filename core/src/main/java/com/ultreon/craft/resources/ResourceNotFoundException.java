package com.ultreon.craft.resources;

import com.ultreon.libs.commons.v0.Identifier;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Identifier id) {
        super(id.toString());
    }
}
