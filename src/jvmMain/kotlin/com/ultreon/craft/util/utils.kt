package com.ultreon.craft.util

import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.ultreon.craft.*
import com.ultreon.craft.resources.*
import kotlinx.serialization.json.*
import kotlin.math.*

val productJson = runBlockingNoJs { readProdJson("assets/craft/product.json") }
val quadIndices = listOf(4, 3, 2, 4, 2, 1)

private suspend fun readProdJson(path: String): ProductJson {
    return Json.Default.decodeFromString(ProductJson.serializer(), resourcesVfs[path].readString())
}

class Button(text: String, handler: suspend () -> Unit) : Container() {
	val textField = Text(text, textSize = 32.0).apply { smoothing = false }
	private val bounds = textField.textBounds
	val g = Graphics().apply {
		solidRect(0.7, 0.7, Colors.DARKGREY) {
			roundRect(bounds.width + 16, bounds.height + 16, 8.0, 8.0) {
                this@roundRect.x = bounds.x
                this@roundRect.y = bounds.y
            }
		}
	}

	var enabledButton = true
		set(value) {
			field = value
			updateState()
		}
	private var overButton = false
		set(value) {
			field = value
			updateState()
		}

	fun updateState() {
        alpha = when {
            !enabledButton -> 0.3
            overButton -> 1.0
            else -> 0.8
        }
	}

	init {
		//this += this.solidRect(bounds.width, bounds.height, Colors.TRANSPARENT_BLACK)
		this += g.apply {
			mouseEnabled = true
		}
		this += textField.position(8, 8)

		mouse {
			over { overButton = true }
			out { overButton = false }
		}
		onClick {
			if (enabledButton) handler()
		}
		updateState()
	}
}


suspend inline fun <reified T : Scene> SceneContainer.changeToDisablingButtons(buttonContainer: Container) {
	for (child in buttonContainer.children.filterIsInstance<Button>()) {
		//println("DISABLE BUTTON: $child")
		child.enabledButton = false
	}
	try {
		changeTo<T>()
	} finally {
		for (child in buttonContainer.children.filterIsInstance<Button>()) {
			//println("ENABLE BUTTON: $child")
			child.enabledButton = true
		}
	}
}

val Identifier.resource: VfsFile
    get() = resourcesVfs["assets/$namespace/$path"]

fun Vector3.Companion.forward(amount: Float = 1f): Vector3 = Vector3(0f, 0f, amount)
fun Vector3.Companion.backward(amount: Float = 1f): Vector3 = Vector3(0f, 0f, -amount)
fun Vector3.Companion.up(amount: Float = 1f): Vector3 = Vector3(0f, amount, 0f)
fun Vector3.Companion.down(amount: Float = 1f): Vector3 = Vector3(0f, -amount, 0f)
fun Vector3.Companion.right(amount: Float = 1f): Vector3 = Vector3(amount, 0f, 0f)
fun Vector3.Companion.left(amount: Float = 1f): Vector3 = Vector3(-amount, 0f, 0f)

fun Vector3D.Companion.forward(amount: Double = 1.0): Vector3D = Vector3D(0.0, 0.0, amount)
fun Vector3D.Companion.backward(amount: Double = 1.0): Vector3D = Vector3D(0.0, 0.0, -amount)
fun Vector3D.Companion.up(amount: Double = 1.0): Vector3D = Vector3D(0.0, amount, 0.0)
fun Vector3D.Companion.down(amount: Double = 1.0): Vector3D = Vector3D(0.0, -amount, 0.0)
fun Vector3D.Companion.right(amount: Double = 1.0): Vector3D = Vector3D(amount, 0.0, 0.0)
fun Vector3D.Companion.left(amount: Double = 1.0): Vector3D = Vector3D(-amount, 0.0, 0.0)

fun Vector3Int.Companion.forward(amount: Int = 1): Vector3Int = Vector3Int(0, 0, amount)
fun Vector3Int.Companion.backward(amount: Int = 1): Vector3Int = Vector3Int(0, 0, -amount)
fun Vector3Int.Companion.up(amount: Int = 1): Vector3Int = Vector3Int(0, amount, 0)
fun Vector3Int.Companion.down(amount: Int = 1): Vector3Int = Vector3Int(0, -amount, 0)
fun Vector3Int.Companion.right(amount: Int = 1): Vector3Int = Vector3Int(amount, 0, 0)
fun Vector3Int.Companion.left(amount: Int = 1): Vector3Int = Vector3Int(-amount, 0, 0)

fun Point.roundToInt(): Vector2Int {
    return Vector2Int(x.roundToInt(), y.roundToInt())
}
