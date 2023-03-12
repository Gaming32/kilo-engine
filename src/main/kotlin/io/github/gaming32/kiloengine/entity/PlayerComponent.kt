package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.loader.LevelLoader
import io.github.gaming32.kiloengine.model.CollisionType
import io.github.gaming32.kiloengine.model.CollisionTypes
import io.github.gaming32.kiloengine.util.*
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.ode4j.math.DMatrix3
import org.ode4j.math.DVector3
import org.ode4j.ode.DContact
import org.ode4j.ode.DContactGeom
import kotlin.math.atan2

class PlayerComponent(
    entity: Entity, private val startRotation: Vector2fc
) : BaseComponent<PlayerComponent>(Type, entity) {
    companion object Type : ComponentType<PlayerComponent>() {
        val IDENTITY = DMatrix3().setIdentity()

        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) =
            PlayerComponent(entity, data["rotation"]?.asJsonArray?.toVector2f() ?: Vector2f())
    }

    private val startPosition = DVector3(entity.body.position)
    val rotation = Vector2f(startRotation)

    var lastJumpCollidedTime = 0.0
    val jumpNormal = DVector3(0.0, 1.0, 0.0)

    private var targetZAngle = 0f
    private var zAngle = 0f

    override fun destroy() = Unit

    fun kill() {
        entity.body.position = startPosition
        rotation.set(startRotation)
        entity.body.linearVel = DVector3.ZERO
    }

    override fun collideWithMesh(
        collision: CollisionType,
        contact: DContactGeom,
        selfIsG1: Boolean
    ): DContact.DSurfaceParameters? {
        if (collision == CollisionTypes.DEATH) {
            kill()
        } else if (collision == CollisionTypes.WALL || collision == CollisionTypes.FLOOR) {
            lastJumpCollidedTime = glfwGetTime()
            if (collision == CollisionTypes.FLOOR) {
                jumpNormal.set(0.0, 1.0, 0.0)
            } else {
                jumpNormal.set(contact.normal)
                if (!selfIsG1) {
                    jumpNormal.scale(-1.0)
                }
            }
        }
        return super.collideWithMesh(collision, contact, selfIsG1)
    }

    override fun tick() {
        if (entity.body.position.y <= -100) {
            kill()
            return
        }

        entity.body.rotation = IDENTITY

        if (jumpNormal.y < 0.95 && glfwGetTime() - lastJumpCollidedTime < 0.1) {
            val horizAngle = normalizeDegrees(
                (Math.toDegrees(atan2(jumpNormal.z, jumpNormal.x)) - 90) + rotation.y
            )
            if (horizAngle < 0) {
                targetZAngle = 5f
            } else if (horizAngle > 0) {
                targetZAngle = -5f
            }
        } else {
            targetZAngle = 0f
        }
        zAngle = Math.lerp(zAngle, targetZAngle, 0.25f)

        var first = true
        entity.getComponents<CameraComponent>().forEach { camera ->
            if (first) {
                camera.rotation.x = rotation.x
                camera.rotation.z = zAngle
                first = false
            }
            camera.rotation.y = 180 - rotation.y
        }
    }
}
