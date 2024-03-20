package com.ultreon.craft.scala.testmod.init

import com.ultreon.craft.block.Block
import com.ultreon.craft.item.ItemStack
import com.ultreon.craft.registry.{DeferRegistry, DeferredElement, Registries}
import com.ultreon.craft.scala.testmod.Constants

import java.util.function.Supplier
import scala.language.postfixOps

object ModBlocks {
  private final val REGISTER = DeferRegistry.of(Constants.MOD_ID, Registries.BLOCK)

  final val TEST_BLOCK: DeferredElement[Block] = REGISTER.defer("test_block", { () =>
    new Block(new Block.Properties().dropsItems(new ItemStack(ModItems.TEST_ITEM.get())))
  })

  def register(): Unit = {
    REGISTER.register()
  }
}
