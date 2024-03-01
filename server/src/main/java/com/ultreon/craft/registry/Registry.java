package com.ultreon.craft.registry;

import com.google.common.base.Preconditions;
import com.ultreon.craft.LoadingContext;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.util.Identifier;
import com.ultreon.libs.commons.v0.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.ultreon.craft.registry.RegistryKey.ROOT;

public class Registry<T> implements IdRegistry<T> {
    public static final Registry<Registry<?>> REGISTRY = new Registry<>(new Builder<>(new Identifier("registry")), ROOT);
    private static final OrderedMap<RegistryKey<Registry<?>>, Registry<?>> REGISTRIES = new OrderedMap<>();
    private static Logger dumpLogger = (level, msg, t) -> {};
    private static boolean frozen;
    private final OrderedMap<RegistryKey<T>, T> keyMap = new OrderedMap<>();
    private final OrderedMap<T, RegistryKey<T>> valueMap = new OrderedMap<>();
    private final Class<T> type;
    private final Identifier id;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Registry(Builder<T> builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;
        this.key = RegistryKey.registry(this);

        RegistryEvents.REGISTRY_DUMP.listen(this::dumpRegistry);

        Registry.REGISTRIES.put((RegistryKey) this.key, this);
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

    public Identifier id() {
        return this.id;
    }

    public RegistryKey<Registry<T>> key() {
        return this.key;
    }

    @SafeVarargs
    @Deprecated
    public static <T> Registry<T> create(Identifier id, @NotNull T... type) {
        return new Builder<>(id, type).build();
    }

    @SafeVarargs
    public static <T> Builder<T> builder(Identifier id, T... typeGetter) {
        return new Builder<>(id, typeGetter);
    }

    @SafeVarargs
    public static <T> Builder<T> builder(String name, T... typeGetter) {
        return new Builder<>(new Identifier(LoadingContext.get().namespace(), name), typeGetter);
    }

    /**
     * Returns the element id of the given registered instance.
     *
     * @param element the registered instance.
     * @return the element id of it.
     */
    @NotNull
    public Identifier getId(@NotNull T element) {
        Preconditions.checkNotNull(element, "element");

        RegistryKey<T> registryKey = this.valueMap.get(element);
        if (registryKey == null) throw new IllegalStateException("Object is not registered: " + element);
        return registryKey.element();
    }

    /**
     * Returns the element id of the given registered instance.
     *
     * @param element the registered instance.
     * @return the element id of it.
     */
    @Nullable
    @Contract("null -> null")
    public Identifier getIdOrNull(@Nullable T element) {
        if (element == null) return null;

        RegistryKey<T> registryKey = this.valueMap.get(element);
        if (registryKey == null) return null;
        return registryKey.element();
    }

    /**
     * Returns the registry key of the given registered instance.
     *
     * @param element the registered instance.
     * @return the registry key of it.
     */
    public RegistryKey<T> getKey(T element) {
        Preconditions.checkNotNull(element, "element");

        return this.valueMap.get(element);
    }

    /**
     * Returns the registered instance from the given {@link Identifier}
     *
     * @param key the namespaced key.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T get(@NotNull Identifier key) {
        Preconditions.checkNotNull(key, "key");

        T element = this.keyMap.get(RegistryKey.of(this.key, key));
        if (element == null) throw new IllegalStateException("Object is not registered: " + key);
        return element;
    }

    public boolean contains(Identifier rl) {
        return this.keyMap.containsKey(RegistryKey.of(this.key, rl));
    }

    public void dumpRegistry() {
        Registry.getDumpLogger().log("Registry dump: " + this.type.getSimpleName());
        for (Map.Entry<Identifier, T> entry : this.getEntries()) {
            T object = entry.getValue();
            Identifier rl = entry.getKey();

            Registry.getDumpLogger().log("  (" + rl + ") -> " + object);
        }
    }

    /**
     * Register an object.
     *
     * @param id  the resource location.
     * @param element the register item value.
     */
    public void register(Identifier id, T element) {
        Preconditions.checkNotNull(id, "id");

        if (!this.type.isAssignableFrom(element.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + element.getClass() + " expected assignable to " + this.type);

        RegistryKey<T> key = new RegistryKey<>(this.key, id);
        if (this.keyMap.containsKey(key) && !this.isOverrideAllowed())
            throw new IllegalArgumentException("Already registered: " + id);

        this.keyMap.put(key, element);
        this.valueMap.put(element, key);
    }

    public boolean isOverrideAllowed() {
        return this.overrideAllowed;
    }

    public boolean isSyncDisabled() {
        return this.syncDisabled;
    }

    public List<T> getValues() {
        return Collections.unmodifiableList(this.keyMap.valueList());
    }

    public List<Identifier> getIds() {
        return this.keyMap.keyList().stream().map(RegistryKey::element).collect(Collectors.toList());
    }

    public List<RegistryKey<T>> getKeys() {
        return Collections.unmodifiableList(this.keyMap.keyList());
    }

    public Set<Map.Entry<Identifier, T>> getEntries() {
        // I do this because the IDE won't accept dynamic values and keys.
        ArrayList<T> values = new ArrayList<>(this.getValues());
        ArrayList<Identifier> keys = new ArrayList<>(this.getIds());

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
            Registry.getDumpLogger().log("Registry: (" + registry.id() + ") -> {");
            Registry.getDumpLogger().log("  Type: " + registry.getType().getName() + ";");
            for (Map.Entry<Identifier, ?> entry : registry.getEntries()) {
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
    public int getRawId(T element) {
        Preconditions.checkNotNull(element, "element");

        int rawId = this.keyMap.indexOfValue(element);
        if (rawId == -1) throw new IllegalArgumentException("Not registered: " + element);
        return rawId;
    }

    @Override
    public T get(int id) {
        if (id < 0 || id >= this.keyMap.size()) throw new IllegalArgumentException("Invalid raw ID: " + id + " for " + this.key);

        return this.keyMap.valueList().get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(RegistryKey<Registry<T>> key) {
        Preconditions.checkNotNull(key, "key");

        return (Registry<T>) REGISTRIES.get(key);
    }

    public void register(RegistryKey<T> key, T element) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(element, "element");

        if (!this.type.isAssignableFrom(element.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + element.getClass() + " expected assignable to " + this.type);

        if (this.keyMap.containsKey(key) && !this.isOverrideAllowed())
            throw new IllegalArgumentException("Already registered: " + key);

        this.keyMap.put(key, element);
        this.valueMap.put(element, key);
    }

    public T get(RegistryKey<T> key) {
        return this.keyMap.get(key);
    }

    public T random() {
        return this.keyMap.valueList().get(ThreadLocalRandom.current().nextInt(this.keyMap.size()));
    }

    public static class Builder<T> {


        private final Class<T> type;
        private final Identifier id;
        private boolean allowOverride = false;
        private boolean doNotSync = false;

        @SuppressWarnings("unchecked")
        public Builder(Identifier id, T... typeGetter) {
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
