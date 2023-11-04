package com.calmwolfs.islazzifarming.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject

object JsonUtils {
    fun JsonObject.getDoubleOr(key: String, or: Double = 0.0): Double {
        return if (has(key)) get(key).asDouble else or
    }

    fun JsonObject.getStringOr(key: String, or: String = ""): String {
        return if (has(key)) get(key).asString else or
    }

    fun JsonObject.getJsonArrayOr(key: String, or: JsonArray = JsonArray()): JsonArray {
        return if (has(key)) get(key).asJsonArray else or
    }

    fun JsonObject.getJsonObjectOr(key: String, or: JsonObject = JsonObject()): JsonObject {
        return if (has(key)) get(key).asJsonObject else or
    }

    fun JsonObject.getBooleanOr(key: String, or: Boolean = false): Boolean {
        return if (has(key)) get(key).asBoolean else or
    }
}