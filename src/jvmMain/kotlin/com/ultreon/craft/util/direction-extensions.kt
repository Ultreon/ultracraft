package com.ultreon.craft.util


val Direction.vector: Vector3Int
    get() {
        return when (this) {
            Direction.UP -> Vector3Int.up
            Direction.DOWN -> Vector3Int.down
            Direction.RIGHT -> Vector3Int.right
            Direction.LEFT -> Vector3Int.left
            Direction.FORWARDS -> Vector3Int.forward
            Direction.BACKWARDS -> Vector3Int.back
        }
    }
