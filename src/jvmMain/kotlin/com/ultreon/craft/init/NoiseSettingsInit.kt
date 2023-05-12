package com.ultreon.craft.init

import com.ultreon.craft.*
import com.ultreon.craft.registry.*
import com.ultreon.craft.resources.*
import com.ultreon.craft.util.*

object NoiseSettingsInit : ObjectInit<NoiseSettings>() {
    override val register: DelayedRegister<NoiseSettings>
        get() = register(defaultNamespace)

    val defaultNoiseSettings = register.register("default") {
        NoiseSettings(0.01f, 5, Vector2Int(-100, 3400), Vector2Int(0, 0), 0.5f, 0.1f, 0.44f)
    }
    val treeNoiseSettings = register.register("tree") {
        NoiseSettings(0.01f, 1, Vector2Int(300, 5000), Vector2Int(0, 0), 0.01f, 1.2f, 4f)
    }
    val stonePatchLayerNoiseSettings = register.register("stone_patch") {
        NoiseSettings(0.01f, 5, Vector2Int(-48303, 85746), Vector2Int(0, 0), 0.5f, 0.25f, 0.635f)
    }
    val noiseSettingsDomainX = register.register("domain_x") {
        NoiseSettings(0.02f, 3, Vector2Int(600, 350), Vector2Int(0, 0), 0.5f, 1.2f, 5f)
    }
    val noiseSettingsDomainY = register.register("domain_y") {
        NoiseSettings(0.02f, 3, Vector2Int(900, 1500), Vector2Int(0, 0), 0.5f, 1.2f, 5f)
    }
}
