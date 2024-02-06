package com.ultreon.xeox.loader;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.Nullable;

/**
 * Registry API for XeoxJS.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class JSRegistries {
    public Registry<?> registry(ElementID id) {
        return Registries.REGISTRY.getElement(id);
    }
    public Registry<?> registry(String name) {
        return Registries.REGISTRY.getElement(ElementID.parse(name));
    }
    public @Nullable ElementID id(String name) {
        return ElementID.tryParse(name);
    }

    public ElementID id(String namespace, String path) {
        return new ElementID(namespace, path);
    }

    public Registry.Builder<?> createBuilder(ElementID id) {
        return new Registry.Builder<>(id);
        
    }
}
