package com.ultreon.craft.registry;

import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.registry.exception.RegistryException;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Registry<T> {
    public static Logger dumpLogger = (level, msg, t) -> {};
    private static final OrderedMap<Class<?>, Registry<?>> REGISTRIES = new OrderedMap<>();
    private static boolean frozen;
    private final OrderedMap<Identifier, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, Identifier> valueMap = new OrderedMap<>();
    private final Class<T> type;
    private final Identifier id;

    protected Registry(Class<T> clazz, Identifier id) throws IllegalStateException {
        this.id = id;
        this.type = clazz;

        RegistryEvents.REGISTRY_DUMP.listen(this::dumpRegistry);
    }

    public static Collection<Registry<?>> getRegistries() {
        return Registry.REGISTRIES.valueList();
    }

    public static void freeze() {
        Registry.frozen = true;
    }

    public Identifier id() {
        return this.id;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> create(Identifier registryName, @NotNull T... type) {
        Class<T> componentType = (Class<T>) type.getClass().getComponentType();
        if (Registry.REGISTRIES.containsKey(componentType)) {
            throw new IllegalStateException();
        }

        Registry<T> registry = new Registry<>(componentType, registryName);
        Registry.REGISTRIES.put(componentType, registry);

        return registry;
    }

    /**
     * Returns the identifier of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the identifier of it.
     */
    @Nullable
    public Identifier getKey(T obj) {
        return this.valueMap.get(obj);
    }

    /**
     * Returns the registered instance from the given {@link Identifier}
     *
     * @param key the namespaced key.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T getValue(@Nullable Identifier key) {
        if (!this.keyMap.containsKey(key)) {
            throw new RegistryException("Cannot find object for: " + key + " | type: " + this.type.getSimpleName());
        }
        return this.keyMap.get(key);
    }

    public boolean contains(Identifier rl) {
        return this.keyMap.containsKey(rl);
    }

    public void dumpRegistry() {
        Registry.dumpLogger.log("Registry dump: " + this.type.getSimpleName());
        for (Map.Entry<Identifier, T> entry : this.entries()) {
            T object = entry.getValue();
            Identifier rl = entry.getKey();

            Registry.dumpLogger.log("  (" + rl + ") -> " + object);
        }
    }

    /**
     * Register an object.
     *
     * @param rl  the resource location.
     * @param val the register item value.
     */
    public void register(Identifier rl, T val) {
        if (!this.type.isAssignableFrom(val.getClass())) {
            throw new IllegalArgumentException("Not allowed type detected, got " + val.getClass() + " expected assignable to " + this.type);
        }

        this.keyMap.put(rl, val);
        this.valueMap.put(val, rl);
    }

    public List<T> values() {
        return Collections.unmodifiableList(this.keyMap.valueList());
    }

    public List<Identifier> keys() {
        return Collections.unmodifiableList(this.keyMap.keyList());
    }

    public Set<Map.Entry<Identifier, T>> entries() {
        // I do this because the IDE won't accept dynamic values and keys.
        ArrayList<T> values = new ArrayList<>(this.values());
        ArrayList<Identifier> keys = new ArrayList<>(this.keys());

        if (keys.size() != values.size()) throw new IllegalStateException("Keys and values have different lengths.");

        Set<Map.Entry<Identifier, T>> entrySet = new HashSet<>();

        for (int i = 0; i < keys.size(); i++) {
            entrySet.add(new AbstractMap.SimpleEntry<>(keys.get(i), values.get(i)));
        }

        return Collections.unmodifiableSet(entrySet);
    }

    public Class<T> getType() {
        return this.type;
    }

    public static void dump() {
        for (Registry<?> registry : Registry.REGISTRIES.valueList()) {
            Registry.dumpLogger.log("Registry: (" + registry.id() + ") -> {");
            Registry.dumpLogger.log("  Type: " + registry.getType().getName() + ";");
            for (Map.Entry<Identifier, ?> entry : registry.entries()) {
                Object o = null;
                String className = null;
                try {
                    o = entry.getValue();
                    className = o.getClass().getName();
                } catch (Throwable ignored) {

                }

                Registry.dumpLogger.log("  (" + entry.getKey() + ") -> {");
                Registry.dumpLogger.log("    Class : " + className + ";");
                Registry.dumpLogger.log("    Object: " + o + ";");
                Registry.dumpLogger.log("  }");
            }
            Registry.dumpLogger.log("}");
        }
    }

    public boolean isFrozen() {
        return Registry.frozen;
    }
}
