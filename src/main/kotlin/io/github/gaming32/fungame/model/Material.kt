package io.github.gaming32.fungame.model

import io.github.gaming32.fungame.util.Drawable
import io.github.gaming32.fungame.util.ModelBuilder
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D

sealed class Material : Drawable {
    data class Color(val r: Float, val g: Float, val b: Float, val a: Float = 1f) : Material() {
        override fun draw(builder: ModelBuilder) {
            builder.disable(GL_TEXTURE_2D)
            builder.color(r, g, b, /*a*/)
        }
    }

    data class Texture(val path: String, val color: Color = DEFAULT_COLOR) : Material() {
        companion object {
            val DEFAULT_COLOR = Color(1f, 1f, 1f, 1f)
        }

        override fun draw(builder: ModelBuilder) {
            builder.color(color.r, color.g, color.b, /*color.a*/)
            builder.enable(GL_TEXTURE_2D)
            builder.texture(path)
        }
    }
}
