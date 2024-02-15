package com.ultreon.craft.groovy.testmod.init

import com.ultreon.craft.groovy.testmod.GroovyTestMod
import com.ultreon.craft.item.Item
import com.ultreon.craft.registry.DeferRegistry
import com.ultreon.craft.registry.Registries
import com.ultreon.craft.registry.DeferredElement

class ModItems {
    private static final def REGISTER = DeferRegistry.of(GroovyTestMod.MOD_ID, Registries.ITEM)

    static final DeferredElement<Item> TEST_ITEM = REGISTER.defer("test_item") { return new Item(new Item.Properties()) }

    static void register() {
        REGISTER.register()
    }
}
