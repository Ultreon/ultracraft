package com.ultreon.bubbles.gradle

import com.google.gson.annotations.SerializedName

@Suppress("unused")
class VersionList(
    @field:SerializedName("versions")
    val versions: List<String>
)