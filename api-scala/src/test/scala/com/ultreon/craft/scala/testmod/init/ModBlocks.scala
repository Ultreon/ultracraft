package com.ultreon.craft.scala.testmod.init

import com.ultreon.craft.block.Block
import com.ultreon.craft.item.{Item, ItemStack}
import com.ultreon.craft.registry.{DeferRegistry, Registries, DeferredElement}
import com.ultreon.craft.scala.testmod.Constants

object ModBlocks {
  private final val REGISTER = DeferRegistry.of(Constants.MOD_ID, Registries.ITEM)

  final val TEST_BLOCK: DeferredElement[Block] = REGISTER.defer("test_block", { () =>
    new Block(new Block.Properties().dropsItems(new ItemStack(ModItems.TEST_ITEM.get())))
  })

  def register(): Unit = {
    REGISTER.register()
  }
}
