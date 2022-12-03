package io.github.gaming32.fungame.entity

import com.google.gson.JsonObject
import io.github.gaming32.fungame.loader.LevelLoader
import io.github.gaming32.fungame.util.*
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.NULL
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C

class CameraComponent(
    entity: Entity,
    val offset: DVector3C = DVector3(),
    val renderArea: Pair<Vector2f, Vector2f>? = Vector2f(0f, 0f) to Vector2f(1f, 1f),
    val textureResolution: Vector2i? = null,
    textureOut: String? = null,
    val rotation: Vector3f = Vector3f(),
    var fov: Float? = 80f,
    val orthoRange: Pair<Vector2d, Vector2d> = Vector2d(-10.0, -10.0) to Vector2d(10.0, 10.0)
) : BaseComponent<CameraComponent>(Type, entity) {
    companion object Type : ComponentType<CameraComponent>() {
        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) = CameraComponent(
            entity,
            data["offset"]?.asJsonArray?.toDVector3() ?: DVector3(),
            if (data["textureOut"] != null) {
                null
            } else Pair(
                data["screenMin"]?.asJsonArray?.toVector2f() ?: Vector2f(0f, 0f),
                data["screenMax"]?.asJsonArray?.toVector2f() ?: Vector2f(1f, 1f)
            ),
            data["textureResolution"]?.asJsonArray?.toVector2i(),
            data["textureOut"]?.asString,
            data["rotation"]?.asJsonArray?.toVector3f() ?: Vector3f(),
            data["fov"].let { when {
                it == null -> 80f
                it.isJsonNull -> null
                else -> it.asFloat
            } },
            Pair(
                data["orthoMin"]?.asJsonArray?.toVector2d() ?: Vector2d(-10.0, -10.0),
                data["orthoMax"]?.asJsonArray?.toVector2d() ?: Vector2d(10.0, 10.0),
            )
        )
    }

    init {
        require((renderArea != null) != (textureResolution != null)) {
            "renderArea and textureResolution are mutually exclusive"
        }
        require((textureResolution != null) == (textureOut != null)) {
            "textureOut and textureResolution must both be specified"
        }
    }

    val framebuffer: Int
    val texture: Int
    val depthStencilBuffer: Int

    init {
        if (textureResolution != null) {
            texture = TextureManager.genVirtualTexture(textureOut!!)

            framebuffer = glGenFramebuffers()
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)

            glBindTexture(GL_TEXTURE_2D, texture)
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                textureResolution.x,
                textureResolution.y,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                NULL
            )
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0)

            depthStencilBuffer = glGenRenderbuffers()
            glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer)
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, textureResolution.x, textureResolution.y)
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBuffer)

            val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                destroyFramebuffer()
                throw IllegalStateException("Couldn't create framebuffer: " + "0x${status.toString(16)}")
            }
        } else {
            framebuffer = 0
            texture = 0
            depthStencilBuffer = 0
        }
    }

    override fun destroy() = destroyFramebuffer()

    private fun destroyFramebuffer() {
        if (textureResolution != null) {
            glDeleteFramebuffers(framebuffer)
            TextureManager.deleteVirtualTexture(texture)
            glDeleteRenderbuffers(depthStencilBuffer)
        }
    }
}
