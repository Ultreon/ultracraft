package com.ultreon.craft.recipe;

import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.registry.AbstractRegistry;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.util.PagedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeRegistry<T extends Recipe> extends AbstractRegistry<ElementID, T> {
    private final OrderedMap<ElementID, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, ElementID> valueMap = new OrderedMap<>();

    public RecipeRegistry(T... typeGetter) {

    }

    @Override
    public T get(ElementID obj) {
        return this.keyMap.get(obj);
    }

    @Override
    public void register(ElementID key, T val) {
        this.keyMap.put(key, val);
        this.valueMap.put(val, key);
    }

    @Override
    public List<T> values() {
        return this.keyMap.valueList();
    }

    @Override
    public Set<ElementID> keys() {
        return Set.copyOf(this.keyMap.keyList());
    }

    @Override
    public Set<Map.Entry<ElementID, T>> entries() throws IllegalAccessException {
        return this.keyMap.entrySet();
    }

    public PagedList<T> getRecipes(int pageSize, @Nullable Inventory inventory) {
        List<T> values = this.keyMap.valueList();
        if (inventory != null) {
            values = values.stream().filter(t -> t.canCraft(inventory)).collect(Collectors.toList());
        }
        return new PagedList<>(pageSize, values);
    }

    public ElementID getKey(T recipe) {
        return this.valueMap.get(recipe);
    }
}
