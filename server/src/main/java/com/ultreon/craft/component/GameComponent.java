package com.ultreon.craft.component;

import com.ultreon.libs.commons.v0.Identifier;

public abstract class GameComponent<T> {
    private final Identifier id;
    private final Class<? extends T> holder;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public GameComponent(Identifier id, T... typeGetter) {
        this.id = id;
        this.holder = (Class<? extends T>) typeGetter.getClass().getComponentType();
    }

    public Class<? extends T> getHolder() {
        return holder;
    }

    public Identifier getId() {
        return id;
    }
}
