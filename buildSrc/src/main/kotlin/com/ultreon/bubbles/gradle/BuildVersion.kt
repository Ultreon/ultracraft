package com.ultreon.bubbles.gradle

import com.google.gson.annotations.SerializedName
import java.util.*

@Suppress("unused")
class BuildVersion(
    @field:SerializedName("project_version")
    val version: String,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("build-date")
    val buildDate: Date,

    @field:SerializedName("type")
    val type: VersionType,

    @field:SerializedName("dependencies")
    val dependencies: List<Dependency>,
)