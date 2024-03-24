package com.ultreon.craft.scala

import net.fabricmc.api.ModInitializer

import scala.annotation.unused

@unused
class ScalaApiMod extends ModInitializer {
  override def onInitialize(): Unit = {
    println("Hello from Scala!")
  }
}
