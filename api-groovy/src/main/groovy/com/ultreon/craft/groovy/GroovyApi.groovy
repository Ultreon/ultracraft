package com.ultreon.craft.groovy

import com.ultreon.craft.client.UltracraftClient
import com.ultreon.craft.server.UltracraftServer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

class GroovyApi {
    def fabricLoader = FabricLoader.instance
    def logger

    def client = { -> UltracraftClient.get() }
    def server = { -> UltracraftServer.get() }

    private def scriptName

    GroovyApi(scriptName) {
        this.scriptName = scriptName
        logger = LoggerFactory.getLogger("Groovy:${scriptName}")
    }

    def getScriptName() { scriptName }
}
