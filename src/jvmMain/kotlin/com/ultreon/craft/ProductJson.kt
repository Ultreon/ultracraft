package com.ultreon.craft

import kotlinx.serialization.Serializable

@Serializable
data class ProductJson(
    val id: String,
    val name: String,
    val gameName: String,
    val gameVersion: String,
    val buildDate: Long,
)
