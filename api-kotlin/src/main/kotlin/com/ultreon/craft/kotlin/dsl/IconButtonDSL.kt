package com.ultreon.craft.kotlin.dsl

import com.ultreon.craft.client.gui.icon.Icon
import com.ultreon.craft.client.gui.widget.IconButton

class IconButtonDSL(val icon: Icon) {
    private var position: () -> PositionDSL = { PositionDSL() }

    infix fun position(dsl: PositionDSL.() -> Unit) {
        this.position = { PositionDSL().apply(dsl) }
    }

    internal fun build() = IconButton.of(icon).position(position)
}
