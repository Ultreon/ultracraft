package com.ultreon.craft.gui

import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korge3d.*
import com.soywiz.korge3d.tween.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import com.ultreon.craft.resources.*
import com.ultreon.craft.util.*
import com.ultreon.craft.UltreonCraft.Companion.instance as game

@Korge3DExperimental
class GameScene : Scene() {
	override suspend fun SContainer.sceneInit() {
		val dirtTex = craftId("textures/dirt.png").resource.readNativeImage().mipmaps(false)
        var dirtMat = Material3D(diffuse = Material3D.LightTexture(dirtTex))
        dirtTex.context2d(false) {
            dirtMat = Material3D(diffuse = Material3D.LightTexture(dirtTex))
        }

		scene3D {
            val perspectiveCamera = camera as PerspectiveCamera3D

            mouseEnabled = false

			perspectiveCamera.set(fov = 60.degrees, near = 0.01, far = 10000.0)

			light().position(0, 0, -3)

            cube(1, 1, 1).position(0, 0, 0).material(dirtMat)

            keys {
                this.downFrame(Key.W) {
                    camera.forward(game.player.speed, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.S) {
                    camera.backwards(game.player.speed, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.A) {
                    camera.strafeLeft(game.player.speed, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.D) {
                    camera.strafeRight(game.player.speed, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.SPACE) {
                    camera.position.offset(Vector3.up(game.player.speed))
                }
                this.downRepeating(Key.LEFT_SHIFT) {
                    camera.moveBy(Vector3.down(game.player.speed))
                }
                this.downFrame(Key.UP) {
                    camera.pitchUp(25.degrees, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.DOWN) {
                    camera.pitchDown(25.degrees, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.RIGHT) {
                    camera.yawRight(25.degrees, it.deltaTime.seconds.toFloat())
                }
                this.downFrame(Key.LEFT) {
                    camera.yawLeft(25.degrees, it.deltaTime.seconds.toFloat())
                }
            }

            mouse {
                onMove {
                    val point = it.currentPosStage - it.lastPosStage
                    camera.yawRight(point.x.degrees, 0.5f)
                    camera.pitchUp(point.y.degrees, 0.5f)
                    input.setMouseGlobalXY(width / 2.0, height / 2.0)
                }
            }

			var tick = 0
			addUpdater {
				val angle = (tick / 4.0).degrees
//				camera.positionLookingAt(
//					cos(angle * 2) * 4, cos(angle * 3) * 4, -sin(angle) * 4, // Orbiting camera
//					0.0, 1.0, 0.0
//				)
				tick++
			}
		}
	}
}

private fun MVector3.offset(offset: Vector3) {
    this += offset
}

operator fun MVector3.plusAssign(offset: Vector3) {
    this.x += offset.x
    this.y += offset.y
    this.z += offset.z
}

operator fun MVector3.minusAssign(offset: Vector3) {
    this.x -= offset.x
    this.y -= offset.y
    this.z -= offset.z
}

operator fun MVector3.timesAssign(offset: Vector3) {
    this.x *= offset.x
    this.y *= offset.y
    this.z *= offset.z
}

operator fun MVector3.divAssign(offset: Vector3) {
    this.x /= offset.x
    this.y /= offset.y
    this.z /= offset.z
}

operator fun MVector3.remAssign(offset: Vector3) {
    this.x %= offset.x
    this.y %= offset.y
    this.z %= offset.z
}

@Korge3DExperimental
private suspend fun Camera3D.moveBy(delta: Vector3, timeSpan: TimeSpan = 1.seconds, easing: Easing = Easing.EASE_IN_OUT_QUAD) {
    moveBy(delta.x, delta.y, delta.z, timeSpan, easing)
}
