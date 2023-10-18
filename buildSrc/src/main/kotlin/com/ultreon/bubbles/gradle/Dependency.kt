package com.ultreon.bubbles.gradle

import com.google.gson.annotations.SerializedName
import java.net.URL

@Suppress("unused")
class Dependency @JvmOverloads constructor(
    @field:SerializedName("group")
    private val group: String,

    @field:SerializedName("name")
    private val name: String,

    @field:SerializedName("project_version")
    private val version: String,

    @field:SerializedName("platform")
    private val platform: Platform,

    @field:SerializedName("classifier")
    private val classifier: String? = null,

    @field:SerializedName("ext")
    private val extension: String = "jar",

    @field:SerializedName("repository")
    private val repository: String
) {
    val url: URL
        get() = URL("$repository/${group.replace(".", "/")}/$name/$version/$name-$version.$extension")
}