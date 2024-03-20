package com.ultreon.craft.scala.testmod.client

import com.ultreon.craft.client.api.events.ClientLifecycleEvents
import com.ultreon.craft.client.{ClientModInit, UltracraftClient}
import net.fabricmc.api.ModInitializer

class ClientScalaTestMod extends ClientModInit {
  override def onInitializeClient(): Unit = {
    ClientLifecycleEvents.CLIENT_STARTED.subscribe((_: UltracraftClient) => {
      println("Game loaded!")
    })
  }
}
