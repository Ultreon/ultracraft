package com.ultreon.craft.registry

import com.ultreon.craft.*
import com.ultreon.craft.resources.*
import kotlin.reflect.*

object Registries {
    val noiseSettings = registry<NoiseSettings>()
    val registries = registry<Registry<*>>()

    fun <T : Any> getRegistry(kClass: KClass<T>): Registry<T> {
        return Registry[kClass]
    }

}
