package com.ultreon.craft.kotlin.dsl

import com.ultreon.craft.client.gui.Position

class PositionDSL : Position() {
    private var position: () -> PositionDSL? = { PositionDSL() }

    var x: Int
        get() = super.x
        set(value) {
            super.x = value
        }
    var y: Int
        get() = super.y
        set(value) {
            super.y = value
        }
}
