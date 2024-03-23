package com.ultreon.craft.tags;

import com.google.common.collect.Lists;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.resources.ReloadContext;
import com.ultreon.craft.resources.Resource;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Identifier;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NamedTag<T> {
    private final Identifier name;
    private final Registry<T> registry;
    private final List<T> values;
    private boolean loaded;

    public NamedTag(Identifier name, Registry<T> registry) {
        this.name = name;
        this.registry = registry;
        this.values = Lists.newArrayList();
    }

    public void reload(ReloadContext context) {
        ResourceManager resourceManager = context.getResourceManager();
        Resource res = resourceManager.getResource(name);
        if (res == null) {
            CommonConstants.LOGGER.warn("Tag not found: " + name + " for registry " + registry.id());
            this.loaded = false;
            return;
        }
        Json5Element rootElem = res.loadJson5();

        if (!(rootElem instanceof Json5Object root)) {
            return;
        }

        for (Json5Element elem : root.getAsJson5Array("elements")) {
            if (!elem.isJson5Primitive() || !elem.getAsJson5Primitive().isString()) {
                continue;
            }

            String element = elem.getAsString();
            if (!element.startsWith("#")) {
                T e = registry.get(new Identifier(element));
                if (e == null) {
                    throw new IllegalArgumentException("Element not found: " + element + " for registry " + registry.id() + " in tag " + name);
                }
                values.add(e);
                continue;
            }

            Optional<NamedTag<T>> tag = registry.getTag(new Identifier(element.substring(1)));
            values.addAll(tag.map(NamedTag::getValues).orElseGet(() -> {
                NamedTag<T> namedTag = new NamedTag<>(new Identifier(element.substring(1)), registry);
                namedTag.reload(context);
                return namedTag.getValues();
            }));
        }

        this.loaded = true;
    }

    public Identifier getName() {
        return name;
    }

    public Collection<T> getValues() {
        if (!loaded) {
            throw new IllegalStateException("Tag not loaded or failed to load: " + name);
        }
        return Collections.unmodifiableCollection(values);
    }

    public boolean contains(T value) {
        if (!loaded) {
            throw new IllegalStateException("Tag not loaded or failed to load: " + name);
        }
        return values.contains(value);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedTag<?> that = (NamedTag<?>) o;
        return name.equals(that.name) && values.equals(that.values);
    }

    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    public String toString() {
        return "Tag[" + name + "]";
    }
}
