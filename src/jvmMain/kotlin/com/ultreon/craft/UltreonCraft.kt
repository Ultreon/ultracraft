package com.ultreon.craft

import com.soywiz.korge.scene.KorgeModule
import com.soywiz.korge3d.Korge3DExperimental
import com.soywiz.korgw.*
import com.soywiz.korim.color.RGBA
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.SizeInt
import com.ultreon.craft.gui.*
import com.ultreon.craft.player.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.*

@Korge3DExperimental
class UltreonCraft : KorgeModule(HUD::class) {
    private var player1: Player? = Player()
    val player: Player
        get() = requireNotNull(player1)
    var world: World? = null
    override val size: SizeInt = SizeInt(1280, 720)
	override val title: String = "${productJson.gameName} v${productJson.gameVersion}"
	override val bgcolor: RGBA = RGBA.float(.25f, .25f, .25f, 1f)
    override val icon: String = "assets/craft/icon.bmp"
    override val quality: GameWindow.Quality
        get() = GameWindow.Quality.QUALITY

    init {

    }

	override suspend fun AsyncInjector.configure() {
		mapPrototype { HUD() }
		mapPrototype { GameScene() }
	}

    fun startGame() {
        world = World()
    }

    companion object {
        val instance = UltreonCraft()
    }
}
