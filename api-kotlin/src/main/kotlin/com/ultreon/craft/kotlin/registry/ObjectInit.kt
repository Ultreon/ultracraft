package com.ultreon.craft.kotlin.registry

import com.ultreon.craft.registry.DeferRegistry
import com.ultreon.craft.registry.DeferredElement
import com.ultreon.craft.registry.Registry

abstract class ObjectInit<T>(namespace: String, registry: Registry<T>) {
    private val register: DeferRegistry<T> = DeferRegistry.of(namespace, registry)

    protected fun <C : T & Any> register(name: String?, supplier: () -> C): DeferredElement<C> {
        return register.defer(name!!) {
            supplier()
        }
    }

    fun register() {
        register.register()
    }
}