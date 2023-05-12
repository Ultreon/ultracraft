package com.ultreon.craft.event

class Event<T : Any>(val builder: (List<T>) -> T) {
    private val handlers: MutableList<T> = mutableListOf()

    val invoker
        get() = builder(handlers)

    operator fun plusAssign(listener: T) {
        handlers += listener;
    }

    operator fun minusAssign(listener: T) {
        handlers -= listener;
    }
}
