package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.MouseMoveEvent
import io.github.gaming32.kiloengine.loader.LevelLoader
import io.github.gaming32.kiloengine.model.CollisionType
import io.github.gaming32.kiloengine.model.CollisionTypes
import io.github.gaming32.kiloengine.util.*
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3d
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.lwjgl.nanovg.NanoVG.nvgText
import org.ode4j.math.DMatrix3
import org.ode4j.math.DVector3
import org.ode4j.ode.DContact
import org.ode4j.ode.DContactGeom
import java.text.DecimalFormat
import kotlin.math.atan2

open class PlayerComponent(
    entity: Entity, private val startRotation: Vector2fc
) : BaseComponent<PlayerComponent>(Type, entity) {
    companion object Type : ComponentType<PlayerComponent>() {
        val IDENTITY = DMatrix3().setIdentity()!!

        private const val MOUSE_SPEED = 0.25
        private const val MOVE_SPEED = 85.0
        private const val JUMP_SPEED = 500.0
        private const val WALL_JUMP_HORIZONTAL = 1500.0
        private const val WALL_JUMP_VERTICAL = 500.0

        private val UI_DEC_FORMAT = DecimalFormat("0.0")

        override fun create(entity: Entity, loader: LevelLoader, data: JsonObject) =
            PlayerComponent(entity, data["rotation"]?.asJsonArray?.toVector2f() ?: Vector2f())
    }

    private val startPosition = DVector3(entity.body.position)
    val rotation = Vector2f(startRotation)

    var lastJumpCollidedTime = 0.0
    val jumpNormal = DVector3(0.0, 1.0, 0.0)

    private var targetZAngle = 0f
    private var zAngle = 0f

    val uiForce = DVector3()

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

    override fun handleMovement(movementInput: Vector3d) {
        val adjustedMovementInput = Vector3d(movementInput).rotateY(
            Math.toRadians(rotation.y.toDouble())
        )
        entity.body.addForce(adjustedMovementInput.x * MOVE_SPEED, 0.0, adjustedMovementInput.z * MOVE_SPEED)
        if (movementInput.y > 0.0 && glfwGetTime() - lastJumpCollidedTime <= 0.1) {
            if (jumpNormal.y < 0.95) {
                entity.body.addForce(
                    0.0,
                    movementInput.y * (WALL_JUMP_VERTICAL - entity.body.linearVel.y),
                    0.0
                )
                entity.body.addForce(DVector3(jumpNormal).scale(movementInput.y * WALL_JUMP_HORIZONTAL))
            } else {
                entity.body.addForce(0.0, movementInput.y * JUMP_SPEED, 0.0)
            }
        }
        uiForce.set(entity.body.force)
    }

    override fun drawUi(nanovg: Long) {
        nvgText(
            nanovg, 10f, 55f,
            "X/Y/Z: " +
                "${UI_DEC_FORMAT.format(entity.body.position.x)}/" +
                "${UI_DEC_FORMAT.format(entity.body.position.y)}/" +
                UI_DEC_FORMAT.format(entity.body.position.z)
        )
        nvgText(
            nanovg, 10f, 75f,
            "FX/FY/FZ: " +
                "${UI_DEC_FORMAT.format(uiForce.x)}/" +
                "${UI_DEC_FORMAT.format(uiForce.y)}/" +
                UI_DEC_FORMAT.format(uiForce.z)
        )
        nvgText(
            nanovg, 10f, 95f,
            "VX/VY/VZ: " +
                "${UI_DEC_FORMAT.format(entity.body.linearVel.x)}/" +
                "${UI_DEC_FORMAT.format(entity.body.linearVel.y)}/" +
                UI_DEC_FORMAT.format(entity.body.linearVel.z)
        )
        nvgText(
            nanovg, 10f, 115f,
            "RY/RX: " +
                "${UI_DEC_FORMAT.format(rotation.y)}/" +
                UI_DEC_FORMAT.format(rotation.x)
        )
    }

    override fun mouseMoved(event: MouseMoveEvent) {
        rotation.y = normalizeDegrees(
            rotation.y - (event.relX * MOUSE_SPEED).toFloat()
        )
        rotation.x = Math.clamp(
            -90f, 90f,
            rotation.x + (event.relY * MOUSE_SPEED).toFloat()
        )
    }
}
