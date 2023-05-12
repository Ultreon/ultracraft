package com.ultreon.craft.util

import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlin.math.*

interface IVector3Int {
    val x: Int
    val y: Int
    val z: Int
}

operator fun IVector3Int.get(index: Int) = when (index) {
    0 -> x
    1 -> y
    2 -> z
    else -> 0f
}

interface MVector3Int : IVector3Int {
    override var x: Int
    override var y: Int
    override var z: Int
}

operator fun MVector3Int.set(index: Int, value: Int) {
    when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
    }
}

fun vec(x: Int, y: Int, z: Int) = Vector3Int(x, y, z)

class Vector3Int : MVector3Int {
    val data: IntArray = IntArray(3)

    override var x: Int get() = data[0]; set(value) { data[0] = value }
    override var y: Int get() = data[1]; set(value) { data[1] = value }
    override var z: Int get() = data[2]; set(value) { data[2] = value }

    val lengthSquared: Int get() = (x * x) + (y * y) + (z * z)
    val length: Float get() = sqrt(lengthSquared.toFloat())

    operator fun get(index: Int): Int = data[index]
    operator fun set(index: Int, value: Int) { data[index] = value }

    companion object {
        val forward = Vector3Int(0, 0, 1)
        val back = Vector3Int(0, 0, -1)
        val up = Vector3Int(0, 1, 0)
        val down = Vector3Int(0, -1, 0)
        val right = Vector3Int(1, 0, 0)
        val left = Vector3Int(-1, 0, 0)

        operator fun invoke(x: Int, y: Int, z: Int): Vector3Int = Vector3Int().setTo(x, y, z)
        operator fun invoke(x: Double, y: Double, z: Double): Vector3Int = Vector3Int().setTo(x, y, z)
        operator fun invoke(x: Float, y: Float, z: Float): Vector3Int = Vector3Int().setTo(x, y, z)

        fun length(x: Double, y: Double, z: Double): Double = sqrt(lengthSq(x, y, z))
        fun length(x: Int, y: Int, z: Int): Float = sqrt(lengthSq(x, y, z).toFloat())

        fun lengthSq(x: Double, y: Double, z: Double): Double = x * x + y * y + z * z
        fun lengthSq(x: Int, y: Int, z: Int): Int = x * x + y * y + z * z
    }

    fun setTo(x: Int, y: Int, z: Int): Vector3Int {
        this.x = x
        this.y = y
        this.z = z
        return this
    }
    fun setTo(x: Double, y: Double, z: Double): Vector3Int = setTo(x.toInt(), y.toInt(), z.toInt())
    fun setTo(x: Float, y: Float, z: Float): Vector3Int = setTo(x.toInt(), y.toInt(), z.toInt())

    inline fun setToFunc(func: (index: Int) -> Int): Vector3Int = setTo(func(0), func(1), func(2))
    inline fun setToFunc(l: Vector3D, r: Vector3D, func: (l: Float, r: Float) -> Int) = setTo(
        func(l.x, r.x),
        func(l.y, r.y),
        func(l.z, r.z),
    )
    fun setToInterpolated(left: Vector3D, right: Vector3D, t: Double): Vector3Int = setToFunc { t.interpolate(left[it], right[it])
        .toInt() }

    fun copyFrom(other: Vector3Int) = setTo(other.x, other.y, other.z)

    fun scale(scale: Int) = this.setTo(this.x * scale, this.y * scale, this.z * scale)
    fun scale(scale: Float) = scale(scale.toInt())
    fun scale(scale: Double) = scale(scale.toInt())

    fun normalize(vector: Vector3Int = this): Vector3Int {
        val norm = 1.0 / vector.length
        setTo(vector.x * norm, vector.y * norm, vector.z * norm)
        return this
    }

    fun normalized(out: Vector3Int = Vector3Int()): Vector3Int = out.copyFrom(this).normalize()

    fun dot(v2: Vector3D): Float = this.x*v2.x + this.y*v2.y + this.z*v2.y

    operator fun plus(that: Vector3Int) = Vector3Int(this.x + that.x, this.y + that.y, this.z + that.z)
    operator fun minus(that: Vector3Int) = Vector3Int(this.x - that.x, this.y - that.y, this.z - that.z)
    operator fun times(scale: Int) = Vector3Int(x * scale, y * scale, z * scale)

    fun sub(l: Vector3Int, r: Vector3Int) = setTo(l.x - r.x, l.y - r.y, l.z - r.z)
    fun add(l: Vector3Int, r: Vector3Int) = setTo(l.x + r.x, l.y + r.y, l.z + r.z)
    fun cross(a: Vector3Int, b: Vector3Int) = setTo(
        (a.y * b.z - a.z * b.y),
        (a.z * b.x - a.x * b.z),
        (a.x * b.y - a.y * b.x),
    )

    fun clone() = Vector3Int(x, y, z)

    override fun equals(other: Any?): Boolean = (other is Vector3Int) && this.x == other.x && this.y == other.y && this.z == other.z
    override fun hashCode(): Int = data.contentHashCode()

    override fun toString(): String = "($x, $y, $z)"
}
