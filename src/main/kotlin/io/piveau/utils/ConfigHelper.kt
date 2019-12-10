package io.piveau.utils

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class ConfigHelper private constructor (private val config: JsonObject) {
    companion object {
        @JvmStatic
        fun forConfig(config: JsonObject): ConfigHelper = ConfigHelper(config)
    }

    @Deprecated("Renamed. Use forceJsonObject instead.")
    fun getJson(key: String): JsonObject = config.forceJsonObject(key)

    fun forceJsonObject(key: String): JsonObject = config.forceJsonObject(key)
    fun forceJsonArray(key: String): JsonArray = config.forceJsonArray(key)
}

fun JsonObject.forceJsonObject(key: String): JsonObject = when(val value = this.getValue(key)) {
    is JsonObject -> value
    is String -> JsonObject(value)
    else -> JsonObject()
}

fun JsonObject.forceJsonArray(key: String): JsonArray = when(val value = this.getValue(key)) {
    is JsonArray -> value
    is String -> JsonArray(value)
    else -> JsonArray()
}
