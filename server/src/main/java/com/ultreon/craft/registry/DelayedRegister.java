package com.ultreon.craft.registry;

import com.google.common.base.Preconditions;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class DelayedRegister<T> {
    @NotNull
    private final String modId;
    @NotNull
    private final Registry<T> registry;
    private final ArrayList<HashMap.Entry<Identifier, Supplier<T>>> objects = new ArrayList<>();

    protected DelayedRegister(@NotNull String modId, @NotNull Registry<T> registry) {
        Preconditions.checkNotNull(modId, "modId");
        this.modId = modId;
        this.registry = registry;
    }

    public static <T> DelayedRegister<T> create(String namespace, Registry<T> registry) {
        return new DelayedRegister<>(namespace, registry);
    }

    public <C extends T> RegistrySupplier<C> register(@NotNull String name, @NotNull Supplier<@NotNull C> supplier) {
        var id = new Identifier(this.modId, name);

        this.objects.add(new HashMap.SimpleEntry<>(id, supplier::get));

        return new RegistrySupplier<>(this.registry, supplier, id);
    }

    public void register() {
        RegistryEvents.AUTO_REGISTER.listen((modId, registry) -> {
            if (!registry.getType().equals(this.registry.getType()) || !this.modId.equals(modId)) {
                return;
            }

            for (HashMap.Entry<Identifier, Supplier<T>> entry : this.objects) {
                T object = entry.getValue().get();
                Identifier id = entry.getKey();

                if (!registry.getType().isAssignableFrom(object.getClass())) {
                    throw new IllegalArgumentException("Got invalid type in deferred register: " + object.getClass() + " expected assignable to " + registry.getType());
                }

                this.registry.register(id, object);
            }
        });
    }
}
