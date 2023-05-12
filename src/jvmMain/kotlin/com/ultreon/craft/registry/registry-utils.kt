package com.ultreon.craft.registry

import kotlin.reflect.*

inline fun <reified T : Any>register(namespace: String): DelayedRegister<T> {
    return DelayedRegister(namespace, Registries.getRegistry(T::class))
}

inline fun <reified T : Any>registry(vararg type: T): Registry<T> {
    return Registry(T::class)
}
