package com.ultreon.craft.registry

import com.ultreon.craft.event.*
import com.ultreon.craft.resources.*

class DelayedRegister<T : Any>(
    private val namespace: String,
    private val registry: Registry<T>
) {
    private val deffer: MutableMap<String, () -> T> = mutableMapOf()

    init {
        RegistryEvents[registry.type] += { reg ->
            for ((name, func) in deffer) {
                reg[id(namespace, name)] = func()
            }
        }
    }

    fun <C : T>register(name: String, func: () -> C): RegistrySupplier<T> {
        deffer[name] = func
        return RegistrySupplier(registry, id(namespace, name))
    }
}
