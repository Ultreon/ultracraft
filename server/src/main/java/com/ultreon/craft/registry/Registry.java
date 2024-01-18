package com.ultreon.craft.registry;

import com.google.common.base.Preconditions;
import com.ultreon.craft.LoadingContext;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.util.ElementID;
import com.ultreon.libs.commons.v0.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.ultreon.craft.registry.RegistryKey.ROOT;

public class Registry<T> implements IdRegistry<T> {
    public static final Registry<Registry<?>> REGISTRY = new Registry<>(new Builder<>(new ElementID("registry")), ROOT);
    private static final OrderedMap<RegistryKey<Registry<?>>, Registry<?>> REGISTRIES = new OrderedMap<>();
    private static Logger dumpLogger = (level, msg, t) -> {};
    private static boolean frozen;
    private final OrderedMap<RegistryKey<T>, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, RegistryKey<T>> valueMap = new OrderedMap<>();
    private final Class<T> type;
    private final ElementID id;
    private final boolean overrideAllowed;
    private final boolean syncDisabled;
    private final RegistryKey<Registry<T>> key;

    private Registry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        Preconditions.checkNotNull(key, "key");
        this.key = key;
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;

        RegistryEvents.REGISTRY_DUMP.listen(this::dumpRegistry);
    }

    private Registry(Builder<T> builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;
        this.key = RegistryKey.registry(this);

        RegistryEvents.REGISTRY_DUMP.listen(this::dumpRegistry);
    }

    public static Collection<Registry<?>> getRegistries() {
        return Registry.REGISTRIES.valueList();
    }

    public static void freeze() {
        Registry.frozen = true;
    }

    public static Logger getDumpLogger() {
        return dumpLogger;
    }

    public static void setDumpLogger(Logger dumpLogger) {
        Registry.dumpLogger = dumpLogger;
    }

    public ElementID id() {
        return this.id;
    }

    public RegistryKey<Registry<T>> key() {
        return this.key;
    }

    @SafeVarargs
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> create(ElementID id, @NotNull T... type) {
        Registry<T> registry = new Builder<>(id, type).build();
        Registry.REGISTRIES.put(Registry.class.cast(registry).key, registry);

        return registry;
    }

    @SafeVarargs
    public static <T> Builder<T> builder(ElementID id, T... typeGetter) {
        return new Builder<>(id, typeGetter);
    }

    @SafeVarargs
    public static <T> Builder<T> builder(String name, T... typeGetter) {
        return new Builder<>(new ElementID(LoadingContext.get().namespace(), name), typeGetter);
    }

    /**
     * Returns the element id of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the element id of it.
     */
    @Nullable
    public ElementID getId(T obj) {
        return this.valueMap.get(obj).element();
    }

    /**
     * Returns the registry key of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the registry key of it.
     */
    public RegistryKey<T> getKey(T obj) {
        return this.valueMap.get(obj);
    }

    /**
     * Returns the registered instance from the given {@link ElementID}
     *
     * @param key the namespaced key.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T getElement(@Nullable ElementID key) {
        return this.keyMap.get(RegistryKey.of(this.key, key));
    }

    public boolean contains(ElementID rl) {
        return this.keyMap.containsKey(RegistryKey.of(this.key, rl));
    }

    public void dumpRegistry() {
        Registry.getDumpLogger().log("Registry dump: " + this.type.getSimpleName());
        for (Map.Entry<ElementID, T> entry : this.entries()) {
            T object = entry.getValue();
            ElementID rl = entry.getKey();

            Registry.getDumpLogger().log("  (" + rl + ") -> " + object);
        }
    }

    /**
     * Register an object.
     *
     * @param rl  the resource location.
     * @param val the register item value.
     */
    public void register(ElementID rl, T val) {
        if (!this.type.isAssignableFrom(val.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + val.getClass() + " expected assignable to " + this.type);

        RegistryKey<T> key = new RegistryKey<>(this.key, rl);
        if (this.keyMap.containsKey(key) && !this.isOverrideAllowed())
            throw new IllegalArgumentException("Already registered: " + rl);

        this.keyMap.put(key, val);
        this.valueMap.put(val, key);
    }

    public boolean isOverrideAllowed() {
        return this.overrideAllowed;
    }

    public boolean isSyncDisabled() {
        return this.syncDisabled;
    }

    public List<T> values() {
        return Collections.unmodifiableList(this.keyMap.valueList());
    }

    public List<ElementID> ids() {
        return this.keyMap.keyList().stream().map(RegistryKey::element).collect(Collectors.toList());
    }

    public List<RegistryKey<T>> keys() {
        return Collections.unmodifiableList(this.keyMap.keyList());
    }

    public Set<Map.Entry<ElementID, T>> entries() {
        // I do this because the IDE won't accept dynamic values and keys.
        ArrayList<T> values = new ArrayList<>(this.values());
        ArrayList<ElementID> keys = new ArrayList<>(this.ids());

        if (keys.size() != values.size()) throw new IllegalStateException("Keys and values have different lengths.");

        Set<Map.Entry<ElementID, T>> entrySet = new HashSet<>();

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
            Registry.getDumpLogger().log("Registry: (" + registry.id() + ") -> {");
            Registry.getDumpLogger().log("  Type: " + registry.getType().getName() + ";");
            for (Map.Entry<ElementID, ?> entry : registry.entries()) {
                Object o = null;
                String className = null;
                try {
                    o = entry.getValue();
                    className = o.getClass().getName();
                } catch (Exception ignored) {
                    // Do nothing
                }

                Registry.getDumpLogger().log("  (" + entry.getKey() + ") -> {");
                Registry.getDumpLogger().log("    Class : " + className + ";");
                Registry.getDumpLogger().log("    Object: " + o + ";");
                Registry.getDumpLogger().log("  }");
            }
            Registry.getDumpLogger().log("}");
        }
    }

    public boolean isFrozen() {
        return Registry.frozen;
    }

    @Override
    public int getRawId(T object) {
        return this.keyMap.indexOfValue(object);
    }

    @Override
    public T byId(int id) {
        return this.keyMap.valueList().get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(RegistryKey<Registry<T>> key) {
        return (Registry<T>) REGISTRIES.get(key);
    }

    public void register(RegistryKey<T> id, T element) {
        if (!this.type.isAssignableFrom(element.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + element.getClass() + " expected assignable to " + this.type);

        if (this.keyMap.containsKey(id) && !this.isOverrideAllowed())
            throw new IllegalArgumentException("Already registered: " + id);

        this.keyMap.put(id, element);
        this.valueMap.put(element, id);
    }

    public T getElement(RegistryKey<T> key) {
        return this.keyMap.get(key);
    }

    public static class Builder<T> {

        private final Class<T> type;
        private final ElementID id;
        private boolean allowOverride = false;
        private boolean doNotSync = false;

        @SuppressWarnings("unchecked")
        public Builder(ElementID id, T... typeGetter) {
            this.type = (Class<T>) typeGetter.getClass().getComponentType();
            this.id = id;
        }

        public Builder<T> allowOverride() {
            this.allowOverride = true;
            return this;
        }

        public Registry<T> build() {
            return new Registry<>(this);
        }

        public Builder<T> doNotSync() {
            this.doNotSync = true;
            return this;
        }
    }
}
