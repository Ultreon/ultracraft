package com.ultreon.craft.registry

import com.soywiz.kds.*
import com.ultreon.craft.resources.*
import com.ultreon.craft.util.*
import kotlin.native.concurrent.*
import kotlin.reflect.*

class Registry<T : Any>(val type: KClass<T>) {
    private val map: MutableMap<Identifier, T> = mutableMapOf()
    private lateinit var frozen: Any

    init {
        if (type in registries)
            throw IllegalArgumentException("Duplicate registry type: ${type.qualifiedName}")
        registries[type] = this
    }

    operator fun set(id: Identifier, value: T) {
        if (this::frozen.isInitialized) throw IllegalStateException("Registry is frozen!")
        map[id] = value
    }

    operator fun get(id: Identifier): T {
        return map[id] ?: throw IllegalArgumentException("Registry entry with id $id doesn't exist.")
    }

    private fun freeze() {
        if (this::frozen.isInitialized) throw IllegalStateException("Registry is already frozen")
        frozen = Any()
    }

    operator fun contains(id: Identifier): Boolean {
        return id in map
    }

    companion object {
        private val frozen: Singleton<Boolean> = Singleton(false)
        private val registries: MutableMap<KClass<*>, Registry<*>> = mutableMapOf()

        fun freezeAll() {
            registries.values.forEach {
                it.freeze()
            }
            frozen.obj = true
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <T : Any> get(kClass: KClass<T>): Registry<T> {
            return registries[kClass] as Registry<T>
        }
    }
}
