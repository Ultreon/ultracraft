package com.ultreon.craft.component;

import com.ultreon.libs.commons.v0.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public interface GameComponentHolder<T extends GameComponent<?>> {
    default Collection<T> components() {
        return Collections.unmodifiableCollection(this.componentRegistry().values());
    }

    Map<Identifier, T> componentRegistry();

    <T2 extends GameComponent<?>> T2 getComponent(Identifier id, T2[] typeGetter);
}
