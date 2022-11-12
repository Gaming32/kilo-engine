package io.github.gaming32.fungame.util

import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import java.io.InputStream
import java.nio.ByteOrder
import javax.imageio.ImageIO

object TextureManager {
    val getResource: (String) -> InputStream? = object {}::class.java::getResourceAsStream

    private val textures = mutableMapOf<String, Int>()

    fun getTexture(name: String) = textures.computeIfAbsent(name) { key ->
        val tex = glGenTextures()
        glGetError().let { error ->
            if (error != GL_NO_ERROR) {
                throw Exception("Failed to create texture: 0x${error.toString(16)}")
            }
        }
        glBindTexture(GL_TEXTURE_2D, tex)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        val image = ImageIO.read(getResource(key))
        val width = image.width
        val height = image.height
        val rgba = MemoryUtil.memAlloc(width * height * 4).order(ByteOrder.BIG_ENDIAN)
        for (pixel in image.getRGB(0, 0, width, height, null, 0, width)) {
            rgba.putInt((pixel shl 8) or (pixel ushr 24))
        }
        rgba.flip()
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba)
        MemoryUtil.memFree(rgba)
        tex
    }

    fun unload() {
        textures.values.forEach { glDeleteTextures(it) }
        textures.clear()
    }
}
