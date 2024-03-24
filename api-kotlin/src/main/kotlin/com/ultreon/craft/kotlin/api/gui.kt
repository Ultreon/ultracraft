package com.ultreon.craft.kotlin.api


import com.ultreon.craft.client.gui.icon.Icon
import com.ultreon.craft.client.gui.widget.IconButton
import com.ultreon.craft.client.gui.widget.TextButton
import com.ultreon.craft.kotlin.dsl.IconButtonDSL
import com.ultreon.craft.kotlin.dsl.TextButtonDSL
import com.ultreon.craft.text.TextObject

fun button(text: TextObject = TextObject.empty(), dsl: TextButtonDSL.() -> Unit): TextButton = TextButtonDSL(text).apply { dsl(this) }.build()

fun button(icon: Icon, dsl: IconButtonDSL.() -> Unit): IconButton = IconButtonDSL(icon).apply { dsl(this) }.build()
