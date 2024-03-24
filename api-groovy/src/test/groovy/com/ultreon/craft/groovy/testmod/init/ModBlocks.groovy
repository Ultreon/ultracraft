package com.ultreon.craft.groovy.testmod.init

import com.ultreon.craft.groovy.testmod.GroovyTestMod
import com.ultreon.craft.groovy.testmod.block.TestBlock
import com.ultreon.craft.registry.DeferRegistry
import com.ultreon.craft.registry.Registries
import com.ultreon.craft.registry.DeferredElement

class ModBlocks {
    private static final def REGISTER = DeferRegistry.of(GroovyTestMod.MOD_ID, Registries.BLOCK)

    static final DeferredElement<TestBlock> TEST_FUNCTIONAL_BLOCK = REGISTER.defer("test_functional_block") { return new TestBlock() }

    static void register() {
        REGISTER.register()
    }
}
