package com.ultreon.craft.gui

import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.SceneContainer
import com.soywiz.korge.scene.sceneContainer
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.SContainer
import com.soywiz.korge.view.position
import com.soywiz.korge3d.Korge3DExperimental
import com.ultreon.craft.util.Button
import com.ultreon.craft.util.changeToDisablingButtons

@Korge3DExperimental
class HUD : Scene() {
	lateinit var contentSceneContainer: SceneContainer

	override suspend fun SContainer.sceneInit() {
		contentSceneContainer = sceneContainer(views)

		contentSceneContainer.changeTo<GameScene>(this)
	}

	inline fun <reified T : Scene> Container.sceneButton(title: String, x: Int) {
		this += Button(title) { contentSceneContainer.changeToDisablingButtons<T>(this) }
			.position(8 + x * 200, views.virtualHeight - 48)
	}
}
