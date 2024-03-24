package com.ultreon.craft.kotlin.api

import com.ultreon.craft.text.TextObject

val String.literal: TextObject
    get() = TextObject.literal(this)

val String.translation: TextObject
    get() = TextObject.translation(this)

fun String.translation(vararg args: Any): TextObject {
    return TextObject.translation(this, *args)
}
