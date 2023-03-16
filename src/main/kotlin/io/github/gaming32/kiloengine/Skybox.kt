package io.github.gaming32.kiloengine

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.util.getElement

sealed class Skybox {
    data class Cubemap(
        val down: String,
        val up: String,
        val negativeZ: String,
        val positiveZ: String,
        val negativeX: String,
        val positiveX: String
    ) : Skybox() {
        companion object {
            @JvmStatic
            fun relative(
                base: String,
                down: String,
                up: String,
                negativeZ: String,
                positiveZ: String,
                negativeX: String,
                positiveX: String,
            ) = Cubemap(
                "$base/$down",
                "$base/$up",
                "$base/$negativeZ",
                "$base/$positiveZ",
                "$base/$negativeX",
                "$base/$positiveX",
            )

            @JvmStatic
            fun fromJson(obj: JsonObject) = if (obj.has("base")) {
                relative(
                    obj.getElement("base").asString,
                    obj.getElement("down").asString,
                    obj.getElement("up").asString,
                    obj.getElement("negativeZ").asString,
                    obj.getElement("positiveZ").asString,
                    obj.getElement("negativeX").asString,
                    obj.getElement("positiveX").asString,
                )
            } else {
                Cubemap(
                    obj.getElement("down").asString,
                    obj.getElement("up").asString,
                    obj.getElement("negativeZ").asString,
                    obj.getElement("positiveZ").asString,
                    obj.getElement("negativeX").asString,
                    obj.getElement("positiveX").asString,
                )
            }
        }
    }

    data class SolidColor(val r: Float, val g: Float, val b: Float) : Skybox() {
        companion object {
            @JvmStatic
            fun fromJson(arr: JsonArray) = SolidColor(arr[0].asFloat, arr[1].asFloat, arr[2].asFloat)
        }
    }

    object None : Skybox() {
        override fun toString() = "None"
    }

    companion object {
        @JvmStatic
        fun fromJson(data: JsonElement) = when {
            data.isJsonObject -> Cubemap.fromJson(data.asJsonObject)
            data.isJsonArray -> SolidColor.fromJson(data.asJsonArray)
            data.isJsonNull -> None
            else -> throw IllegalArgumentException("Invalid skybox: $data")
        }
    }
}
