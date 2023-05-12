package com.ultreon.craft.registry

abstract class ObjectInit<T : Any> {
    abstract val register: DelayedRegister<T>
}
