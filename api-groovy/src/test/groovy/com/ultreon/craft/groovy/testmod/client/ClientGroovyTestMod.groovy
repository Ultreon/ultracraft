package com.ultreon.craft.groovy.testmod.client

import com.ultreon.craft.client.ClientModInit
import com.ultreon.craft.client.api.events.ClientLifecycleEvents

class ClientGroovyTestMod implements ClientModInit {
    @Override
    void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.subscribe {
            def name = it.user.name()

            println "Hello ${name}!"
        }
    }
}
