package com.ultreon.craft.kotlin.testmod.client

import com.ultreon.craft.client.ClientModInit
import com.ultreon.craft.client.api.events.gui.ScreenEvents
import com.ultreon.craft.client.gui.screens.TitleScreen
import com.ultreon.craft.client.gui.widget.Button
import com.ultreon.craft.client.gui.widget.TextButton
import com.ultreon.craft.events.api.ValueEventResult
import com.ultreon.craft.kotlin.api.button
import com.ultreon.craft.kotlin.api.literal
import com.ultreon.craft.util.Color
import com.ultreon.craft.util.Color.hex
import com.ultreon.craft.util.Color.rgb

class ClientKotlinTestMod : ClientModInit {
    override fun onInitializeClient() {
        println("Hello from Kotlin!")

        ScreenEvents.OPEN.subscribe {
            if (it is TitleScreen) {
                it.add(button {
                    this.text = "Hello".literal
                    this.textColor = rgb(1.0f, 1.0f, 1.0f)

                    this.clicked {
                        println("Hello from Kotlin!")
                    }
                })
            }

            return@subscribe ValueEventResult.pass()
        }
    }

}