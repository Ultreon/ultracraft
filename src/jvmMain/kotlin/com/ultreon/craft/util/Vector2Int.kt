package com.ultreon.craft.util

import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlin.math.*

interface IVector2Int {
    val x: Int
    val y: Int
}

operator fun IVector2Int.get(index: Int) = when (index) {
    0 -> x
    1 -> y
    else -> 0f
}

interface MVector2Int : IVector2Int {
    override var x: Int
    override var y: Int
}

operator fun MVector2Int.set(index: Int, value: Int) {
    when (index) {
        0 -> x = value
        1 -> y = value
    }
}

fun vec(x: Int, y: Int) = Vector2Int(x, y)

class Vector2Int : MVector2Int {
    val data: IntArray = IntArray(3)

    override var x: Int get() = data[0]; set(value) { data[0] = value }
    override var y: Int get() = data[1]; set(value) { data[1] = value }

    val lengthSquared: Int get() = (x * x) + (y * y)
    val length: Float get() = sqrt(lengthSquared.toFloat())

    operator fun get(index: Int): Int = data[index]
    operator fun set(index: Int, value: Int) { data[index] = value }

    companion object {
        operator fun invoke(x: Int, y: Int): Vector2Int = Vector2Int().setTo(x, y)
        operator fun invoke(x: Double, y: Double): Vector2Int = Vector2Int().setTo(x, y)
        operator fun invoke(x: Float, y: Float): Vector2Int = Vector2Int().setTo(x, y)

        fun length(x: Double, y: Double): Double = sqrt(lengthSq(x, y))
        fun length(x: Int, y: Int): Float = sqrt(lengthSq(x, y).toFloat())

        fun lengthSq(x: Double, y: Double): Double = x * x + y * y
        fun lengthSq(x: Int, y: Int): Int = x * x + y * y
    }

    fun setTo(x: Int, y: Int): Vector2Int {
        this.x = x
        this.y = y
        return this
    }
    fun setTo(x: Double, y: Double): Vector2Int = setTo(x.toInt(), y.toInt())
    fun setTo(x: Float, y: Float): Vector2Int = setTo(x.toInt(), y.toInt())

    inline fun setToFunc(func: (index: Int) -> Int): Vector2Int = setTo(func(0), func(1))
    inline fun setToFunc(l: Vector2D, r: Vector2D, func: (l: Double, r: Double) -> Int) = setTo(
        func(l.x, r.x),
        func(l.y, r.y),
    )
    fun setToInterpolated(left: Vector2D, right: Vector2D, t: Double): Vector2Int = setToFunc { t.interpolate(left[it], right[it]).toInt() }

    fun copyFrom(other: Vector2Int) = setTo(other.x, other.y)

    fun scale(scale: Int) = this.setTo(this.x * scale, this.y * scale)
    fun scale(scale: Float) = scale(scale.toInt())
    fun scale(scale: Double) = scale(scale.toInt())

    fun normalize(vector: Vector2Int = this): Vector2Int {
        val norm = 1.0 / vector.length
        setTo(vector.x * norm, vector.y * norm)
        return this
    }

    fun normalized(out: Vector2Int = Vector2Int()): Vector2Int = out.copyFrom(this).normalize()

    fun dot(v2: Vector2D): Double = this.x*v2.x + this.y*v2.y

    operator fun plus(that: Vector2D) = Vector2D(this.x + that.x, this.y + that.y)
    operator fun minus(that: Vector2D) = Vector2D(this.x - that.x, this.y - that.y)
    operator fun times(scale: Int) = Vector2D(x * scale, y * scale)

    fun sub(l: Vector2Int, r: Vector2Int) = setTo(l.x - r.x, l.y - r.y)
    fun add(l: Vector2Int, r: Vector2Int) = setTo(l.x + r.x, l.y + r.y)
    fun cross(a: Vector2Int, b: Vector2Int) = setTo(
        (a.x * b.y - a.y * b.x),
        (a.y * b.x - a.x * b.y),
    )

    fun clone() = Vector2Int(x, y)

    override fun equals(other: Any?): Boolean = (other is Vector2Int) && this.x == other.x && this.y == other.y
    override fun hashCode(): Int = data.contentHashCode()

    override fun toString(): String = "($x, $y)"
}
