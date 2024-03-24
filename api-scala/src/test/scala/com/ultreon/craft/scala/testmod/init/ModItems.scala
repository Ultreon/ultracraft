package com.ultreon.craft.scala.testmod.init

import com.ultreon.craft.item.Item
import com.ultreon.craft.registry.{DeferRegistry, Registries, DeferredElement}
import com.ultreon.craft.scala.testmod.Constants

object ModItems {
  private final val REGISTER = DeferRegistry.of(Constants.MOD_ID, Registries.ITEM)

  final val TEST_ITEM: DeferredElement[Item] = REGISTER.defer("test_item", { () =>
    new Item(new Item.Properties)
  })

  def register(): Unit = {
    REGISTER.register()
  }
}
