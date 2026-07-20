package com.freewdcmkt.bck.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy


object JsonParser {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }
}
