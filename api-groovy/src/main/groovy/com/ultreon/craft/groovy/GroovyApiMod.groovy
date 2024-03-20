package com.ultreon.craft.groovy

import com.ultreon.craft.client.api.events.ClientLifecycleEvents
import com.ultreon.craft.client.api.events.ClientTickEvents
import net.fabricmc.api.ModInitializer

class GroovyApiMod implements ModInitializer {
    static final def MOD_ID = "groovy_api"
    GroovyScriptEngine engine

    @Override
    void onInitialize() {
        println "Hello from Groovy! Mod ID: $MOD_ID"

        engine = new GroovyScriptEngine()

        def hooksDir = new File("groovy-hooks")

        if (hooksDir.exists()) {
            def file = new File(hooksDir, "client-hooks.groovy")
            if (file.exists()) {
                engine.createScript("client-hooks.groovy", new Binding(
                        $this: new GroovyApi("client-hooks.groovy"),
                        clientStarted: ClientLifecycleEvents.CLIENT_STARTED,
                        clientStopped: ClientLifecycleEvents.CLIENT_STOPPED,
                        clientTick: ClientTickEvents.POST_GAME_TICK,
                )).run(file)
            }
        }
    }
}
