package com.ultreon.craft.groovy.testmod.block

import com.ultreon.craft.block.Block
import com.ultreon.craft.groovy.testmod.init.ModItems

class TestBlock extends Block {
    TestBlock() {
        super(new Properties().dropsItems(ModItems.TEST_ITEM))
    }
}
