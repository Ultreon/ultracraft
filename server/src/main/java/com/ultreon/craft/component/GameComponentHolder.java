package com.ultreon.craft.component;

import com.ultreon.craft.util.ElementID;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public interface GameComponentHolder<T extends GameComponent<?>> {
    default Collection<T> components() {
        return Collections.unmodifiableCollection(this.componentRegistry().values());
    }

    Map<ElementID, T> componentRegistry();

    <T2 extends GameComponent<?>> T2 getComponent(ElementID id, T2[] typeGetter);
}
