import com.soywiz.korge.*
import com.soywiz.korge3d.*
import com.ultreon.craft.*

@Korge3DExperimental
suspend fun main() {
    Korge(Korge.Config(module = UltreonCraft.instance, gameWindow = SdlGameWindowJvm(true)))
}

