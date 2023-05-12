package com.ultreon.craft.event

import com.ultreon.craft.registry.*
import kotlin.reflect.*

object RegistryEvents {
    val events = mutableMapOf<KClass<*>, Event<*>>()

    inline operator fun <T : Any>get(kClass: KClass<T>) : Event<RegistryEvent<T>> {
        return getOrCreate(kClass)
    }

    fun <T : Any> getOrCreate(kClass: KClass<T>): Event<RegistryEvent<T>> {
        if (kClass in events) {
            @Suppress("UNCHECKED_CAST")
            return events[kClass] as Event<RegistryEvent<T>>
        }

        val event: Event<RegistryEvent<T>> = Event { handlers ->
            RegistryEvent { reg ->
                for (handler in handlers) {
                    handler(reg)
                }
            }
        }

        events[kClass] = event
        return event
    }

    fun interface RegistryEvent<T : Any> {
        operator fun invoke(registry: Registry<T>)
    }
}
