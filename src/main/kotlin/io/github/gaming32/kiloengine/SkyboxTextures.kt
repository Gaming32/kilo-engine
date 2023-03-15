package io.github.gaming32.kiloengine

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.util.getElement

data class SkyboxTextures(
    val down: String,
    val up: String,
    val negativeZ: String,
    val positiveZ: String,
    val negativeX: String,
    val positiveX: String,
) {
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
        ) = SkyboxTextures(
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
            SkyboxTextures(
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
