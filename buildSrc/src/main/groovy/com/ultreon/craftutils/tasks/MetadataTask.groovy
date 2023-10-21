package com.ultreon.craftutils.tasks

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import com.ultreon.craftutils.CraftUtilsExt
import com.ultreon.craftutils.CraftUtilsPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.time.ZoneOffset

class MetadataTask extends DefaultTask {
    public CraftUtilsExt craftUtils

    @OutputFile
    def metadataFile = project.file("$project.projectDir/build/metadata.json")

    @Inject
    MetadataTask() {
        this.craftUtils = CraftUtilsPlugin.extension
        this.group = "craftutils"
    }

    @TaskAction
    void createJson() {
        def gson = new GsonBuilder().create()
        def writer = new JsonWriter(new FileWriter(metadataFile))
        writer.indent = "  "

        def json = new JsonObject()
        json.addProperty "javaVersion", craftUtils.javaVersion
        json.addProperty "buildDate", craftUtils.buildDate.atOffset(ZoneOffset.UTC).toEpochSecond()
        json.addProperty "version", craftUtils.projectVersion
        gson.toJson json, writer
        writer.flush()
        writer.close()
    }
}
