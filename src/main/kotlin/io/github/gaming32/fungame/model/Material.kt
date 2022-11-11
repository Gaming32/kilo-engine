package io.github.gaming32.fungame.model

import io.github.gaming32.fungame.util.Drawable
import io.github.gaming32.fungame.util.ModelBuilder
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D

sealed class Material : Drawable {
    data class Color(val r: Float, val g: Float, val b: Float, val a: Float? = null) : Material() {
        override fun draw(builder: ModelBuilder) {
            builder.disable(GL_TEXTURE_2D)
            if (a != null) {
                builder.color(r, g, b, a)
            } else {
                builder.color(r, g, b)
            }
        }
    }

    data class Texture(val name: String) : Material() {
        override fun draw(builder: ModelBuilder) {
            builder.color(1f, 1f, 1f, 1f)
            builder.enable(GL_TEXTURE_2D)
            builder.texture(name)
        }
    }
}
