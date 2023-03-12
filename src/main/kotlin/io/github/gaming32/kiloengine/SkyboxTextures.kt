package io.github.gaming32.kiloengine

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
    }
}
