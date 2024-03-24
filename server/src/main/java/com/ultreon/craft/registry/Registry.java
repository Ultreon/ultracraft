package com.ultreon.craft.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ultreon.craft.LoadingContext;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.util.Identifier;
import com.ultreon.libs.commons.v0.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.stream.Collectors;

import static com.ultreon.craft.registry.RegistryKey.ROOT;

public class Registry<T> extends AbstractRegistry<RegistryKey<T>, T> implements RawIdMap<T>, Publisher<Registry<T>> {
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
    private final List<Subscriber<? super Registry<T>>> subscriptions = Lists.newArrayList();

    private Registry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        Preconditions.checkNotNull(key, "key");
        this.key = key;
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;

        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Registry(Builder<T> builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.overrideAllowed = builder.allowOverride;
        this.syncDisabled = builder.doNotSync;
        this.key = RegistryKey.registry(this);

        RegistryEvents.REGISTRY_DUMP.subscribe(this::dumpRegistry);

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
     * @param obj the registered instance.
     * @return the element id of it.
     */
    @Nullable
    public Identifier getId(T obj) {
        RegistryKey<T> registryKey = this.valueMap.get(obj);
        if (registryKey == null) return null;
        return registryKey.element();
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
     * Returns the registered instance from the given {@link Identifier}
     *
     * @param key the element id.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T getElement(@Nullable Identifier key) {
        return this.keyMap.get(RegistryKey.of(this.key, key));
    }

    public boolean contains(Identifier rl) {
        return this.keyMap.containsKey(RegistryKey.of(this.key, rl));
    }

    public void dumpRegistry() {
        Registry.getDumpLogger().log("Registry dump: " + this.type.getSimpleName());
        for (Map.Entry<RegistryKey<T>, T> entry : this.entries()) {
            T object = entry.getValue();
            Identifier rl = entry.getKey().element();

            Registry.getDumpLogger().log("  (" + rl + ") -> " + object);
        }
    }

    /**
     * Register an object.
     *
     * @param rl  the resource location.
     * @param val the register item value.
     */
    public void register(Identifier rl, T val) {
        if (!this.type.isAssignableFrom(val.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + val.getClass() + " expected assignable to " + this.type);

        RegistryKey<T> key = new RegistryKey<>(this.key, rl);
        if (this.keyMap.containsKey(key) && !this.overrideAllowed)
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

    public List<Identifier> ids() {
        return this.keyMap.keyList().stream().map(RegistryKey::element).collect(Collectors.toList());
    }

    public List<RegistryKey<T>> keys() {
        return Collections.unmodifiableList(this.keyMap.keyList());
    }

    public Set<Map.Entry<RegistryKey<T>, T>> entries() {
        // I do this because the IDE won't accept dynamic values and keys.
        ArrayList<T> values = new ArrayList<>(this.values());
        ArrayList<RegistryKey<T>> keys = new ArrayList<>(this.keys());

        if (keys.size() != values.size()) throw new IllegalStateException("Keys and values have different lengths.");

        Set<Map.Entry<RegistryKey<T>, T>> entrySet = new HashSet<>();

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
            for (Map.Entry<? extends RegistryKey<?>, ?> entry : registry.entries()) {
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

        if (this.keyMap.containsKey(id) && !this.overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + id);

        this.keyMap.put(id, element);
        this.valueMap.put(element, id);
    }

    public T get(RegistryKey<T> key) {
        return this.keyMap.get(key);
    }

    @Override
    public void subscribe(Subscriber<? super Registry<T>> s) {
        this.subscriptions.add(s);
        s.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                // Do nothing
            }

            @Override
            public void cancel() {
                Registry.this.unsubscribe(s);
            }
        });
    }

    public void unsubscribe(Subscriber<? super Registry<T>> s) {
        this.subscriptions.remove(s);
    }

    public void publish() {
        for (Subscriber<? super Registry<T>> s : this.subscriptions) {
            s.onNext(this);
            s.onComplete();
        }
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
