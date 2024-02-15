package com.ultreon.craft.groovy.testmod

import com.ultreon.craft.ModInit
import com.ultreon.craft.events.PlayerEvents
import com.ultreon.craft.groovy.testmod.init.ModBlocks
import com.ultreon.craft.groovy.testmod.init.ModItems
import com.ultreon.craft.item.ItemStack

class GroovyTestMod implements ModInit {
    public static def MOD_ID = "groovy_testmod"

    @Override
    void onInitialize() {
        println("Hello from Groovy! Mod ID: ${MOD_ID}")

        ModBlocks.register()
        ModItems.register()

        PlayerEvents.INITIAL_ITEMS.subscribe {
            it.inventory.addItems([new ItemStack(ModItems.TEST_ITEM.get())])
        }
    }
}
