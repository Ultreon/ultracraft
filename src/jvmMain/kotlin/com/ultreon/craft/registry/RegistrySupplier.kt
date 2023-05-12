package com.ultreon.craft.registry

import com.ultreon.craft.resources.*
import com.ultreon.craft.util.*

class RegistrySupplier<T : Any>(
    private val registry: Registry<in T>,
    private val id: Identifier
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): T {
        if (id !in registry)
            throw IllegalArgumentException("Object not registered: $id")
        return registry[id] as T
    }
}
