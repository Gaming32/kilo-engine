package io.github.gaming32.fungame.entity

import com.google.gson.JsonObject
import io.github.gaming32.fungame.loader.LevelLoader
import io.github.gaming32.fungame.util.toDVector3
import io.github.gaming32.fungame.util.toVector2d
import io.github.gaming32.fungame.util.toVector2f
import io.github.gaming32.fungame.util.toVector3f
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C

class CameraComponent(
    entity: Entity,
    val offset: DVector3C = DVector3(),
    val renderArea: Pair<Vector2f, Vector2f>? = Vector2f(0f, 0f) to Vector2f(1f, 1f),
    val rotation: Vector3f = Vector3f(),
    var fov: Float? = 80f,
    val orthoRange: Pair<Vector2d, Vector2d> = Vector2d(-10.0, -10.0) to Vector2d(10.0, 10.0)
) : BaseComponent<CameraComponent>(Type, entity) {
    companion object Type : ComponentType<CameraComponent>() {
        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) = CameraComponent(
            entity,
            data["offset"]?.asJsonArray?.toDVector3() ?: DVector3(),
            if (data["renderToScreen"]?.asBoolean == false) {
                null
            } else Pair(
                data["screenMin"]?.asJsonArray?.toVector2f() ?: Vector2f(0f, 0f),
                data["screenMax"]?.asJsonArray?.toVector2f() ?: Vector2f(1f, 1f)
            ),
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

    internal val lastWindowSize = Vector2i()
    val textureResolution = Vector2f()

    val renderFullscreen = renderArea == (Vector2f(0f, 0f) to Vector2f(1f, 1f))
    var framebuffer = 0
        internal set
    var texture = 0
        internal set
    var depthStencilBuffer = 0
        internal set

    override fun destroy() = destroyFramebuffer()

    internal fun destroyFramebuffer() {
        if (framebuffer != 0) {
            glDeleteFramebuffers(framebuffer)
        }
        if (texture != 0) {
            glDeleteTextures(texture)
        }
        if (depthStencilBuffer != 0) {
            glDeleteRenderbuffers(depthStencilBuffer)
        }
    }
}
