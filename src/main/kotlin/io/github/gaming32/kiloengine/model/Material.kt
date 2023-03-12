package io.github.gaming32.kiloengine.model

import io.github.gaming32.kiloengine.util.Drawable
import io.github.gaming32.kiloengine.util.ModelBuilder
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D

sealed class Material : Drawable {
    data class Color(val r: Float, val g: Float, val b: Float, val a: Float = 1f) : Material() {
        override fun draw(builder: ModelBuilder) {
            builder.disable(GL_TEXTURE_2D)
            builder.color(r, g, b, a)
        }

        override val isFullyTransparent get() = a == 0f
    }

    data class Texture(val path: String, val color: Color = DEFAULT_COLOR) : Material() {
        companion object {
            val DEFAULT_COLOR = Color(1f, 1f, 1f, 1f)
        }

        override fun draw(builder: ModelBuilder) {
            builder.color(color.r, color.g, color.b, color.a)
            builder.enable(GL_TEXTURE_2D)
            builder.texture(path)
        }

        override val isFullyTransparent get() = color.isFullyTransparent
    }

    abstract val isFullyTransparent: Boolean
}
