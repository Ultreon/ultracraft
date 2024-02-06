package com.ultreon.craft.registry;

import com.google.common.base.Preconditions;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class DelayedRegister<T> {
    @NotNull
    private final String modId;
    @NotNull
    private final Registry<T> registry;
    private final ArrayList<HashMap.Entry<ElementID, Supplier<T>>> objects = new ArrayList<>();

    protected DelayedRegister(@NotNull String modId, @NotNull Registry<T> registry) {
        Preconditions.checkNotNull(modId, "modId");
        this.modId = modId;
        this.registry = registry;
    }

    public static <T> DelayedRegister<T> create(String namespace, Registry<T> registry) {
        return new DelayedRegister<>(namespace, registry);
    }

    public <C extends T> RegistrySupplier<C> register(@NotNull String name, @NotNull Supplier<@NotNull C> supplier) {
        var id = new ElementID(this.modId, name);

        this.objects.add(new HashMap.SimpleEntry<>(id, supplier::get));

        return new RegistrySupplier<>(this.registry, supplier, id);
    }

    public void register() {
        RegistryEvents.AUTO_REGISTER.listen((modId, registry) -> {
            if (!registry.key().equals(this.registry.key()) || !this.modId.equals(modId)) {
                return;
            }

            for (HashMap.Entry<ElementID, Supplier<T>> entry : this.objects) {
                T object = entry.getValue().get();
                ElementID id = entry.getKey();

                this.registry.register(id, object);
            }
        });
    }
}
