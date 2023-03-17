package io.github.gaming32.kiloengine

import org.imgscalr.Scalr
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.system.MemoryUtil
import java.nio.ByteOrder
import javax.imageio.ImageIO

object TextureManager {
    var maxMipmap = 4
    var filter = GL11.GL_LINEAR
    var mipmapFilter = GL11.GL_LINEAR_MIPMAP_LINEAR
    var wrap = GL11.GL_REPEAT

    private val textures = mutableMapOf<String, Int>()
    private val virtualTexturesById = mutableMapOf<Int, String>()

    fun genVirtualTexture(name: String): Int {
        val registeredName = "~$name"
        val texture = GL11.glGenTextures()
        textures.put(registeredName, texture)?.also { oldTexture ->
            GL11.glDeleteTextures(texture)
            textures[registeredName] = oldTexture
            throw IllegalArgumentException("Duplicate virtual texture: $name")
        }
        virtualTexturesById[texture] = registeredName
        return texture
    }

    fun deleteVirtualTexture(name: String) {
        val texture = textures.remove("~$name")
            ?: throw IllegalArgumentException("$name is not registered as a virtual texture")
        virtualTexturesById.remove(texture)
        GL11.glDeleteTextures(texture)
    }

    fun deleteVirtualTexture(texture: Int) {
        val registeredName = virtualTexturesById.remove(texture)
            ?: throw IllegalArgumentException("Texture $texture is not associated with a virtual texture")
        textures.remove(registeredName)
        GL11.glDeleteTextures(texture)
    }

    fun getTexture(name: String) = textures.computeIfAbsent(name) { initTexture(it, GL11.glGenTextures()) }

    private fun initTexture(path: String, tex: Int): Int {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex)
        GL11.glTexParameteri(
            GL11.GL_TEXTURE_2D,
            GL11.GL_TEXTURE_MIN_FILTER,
            if (maxMipmap == -1) filter else mipmapFilter
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap)
        if (maxMipmap != -1) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, maxMipmap)
        }
        val image = ImageIO.read(Resources.getResource(path) ?: run {
            GL11.glDeleteTextures(tex)
            throw IllegalArgumentException("Missing texture $path. Did you forget to create a virtual texture *before* it's used?")
        })
        var width = image.width
        var height = image.height
        val rgba = MemoryUtil.memAlloc(width * height * 4).order(ByteOrder.BIG_ENDIAN)
        try {
            for (pixel in image.getRGB(0, 0, width, height, null, 0, width)) {
                rgba.putInt((pixel shl 8) or (pixel ushr 24))
            }
            rgba.flip()
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                rgba
            )
            if (maxMipmap != -1) {
                for (i in 1..maxMipmap) {
                    width /= 2
                    height /= 2
                    for (pixel in Scalr.resize(image, width, height).getRGB(0, 0, width, height, null, 0, width)) {
                        rgba.putInt((pixel shl 8) or (pixel ushr 24))
                    }
                    rgba.flip()
                    GL11.glTexImage2D(
                        GL11.GL_TEXTURE_2D,
                        i,
                        GL11.GL_RGBA,
                        width,
                        height,
                        0,
                        GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE,
                        rgba
                    )
                }
            }
        } finally {
            MemoryUtil.memFree(rgba)
        }
        return tex
    }

    fun unload() {
        textures.values.forEach { GL11.glDeleteTextures(it) }
        textures.clear()
        virtualTexturesById.clear()
    }

    fun loadAsVirtual(texture: String, name: String) = initTexture(texture, genVirtualTexture(name))
}