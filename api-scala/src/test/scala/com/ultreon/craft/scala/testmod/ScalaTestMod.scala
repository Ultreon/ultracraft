package com.ultreon.craft.scala.testmod

import com.ultreon.craft.scala.testmod.init.ModItems
import net.fabricmc.api.ModInitializer

class ScalaTestMod extends ModInitializer {
  override def onInitialize(): Unit = {
    ModItems.register()
  }
}
