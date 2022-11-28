package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.util.toDMatrix3
import org.joml.Math
import org.joml.Matrix3d
import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.OdeHelper

class PlayerEntity(
    level: Level, val startPos: DVector3C, val startRotation: Vector2fc
) : Entity<PlayerEntity>(PlayerEntity, level, OdeHelper.createCapsule(level.space, 0.4, 1.0), startPos) {
    companion object Type : EntityType<PlayerEntity>() {
        val UP = DVector3(0.0, 1.0, 0.0)
        val Z_FORWARD = Matrix3d().rotateX(Math.PI / 2).toDMatrix3()

        override fun create(level: Level, position: DVector3C, args: List<String>) = PlayerEntity(
            level, position,
            Vector2f(args[0].toFloat(), args[1].toFloat())
        )
    }

    val rotation = Vector2f(startRotation)

    init {
        geom.rotation = Z_FORWARD
    }

    var lastJumpCollidedTime = 0.0
    val jumpNormal = DVector3(UP)

    init {
        body.position = startPos
    }

    override fun kill() {
        body.position = startPos
        rotation.set(startRotation)
        body.linearVel = DVector3.ZERO
    }

    override fun collideWithLevel(collision: CollisionType, contact: DContactGeom, levelFirst: Boolean): Boolean {
        val result = super.collideWithLevel(collision, contact, levelFirst)
        if (collision == CollisionTypes.WALL || collision == CollisionTypes.FLOOR) {
            lastJumpCollidedTime = glfwGetTime()
            if (collision == CollisionTypes.FLOOR) {
                jumpNormal.set(0.0, 1.0, 0.0)
            } else {
                jumpNormal.set(contact.normal)
                if (levelFirst) {
                    jumpNormal.scale(-1.0)
                }
            }
        }
        return result
    }

    override fun tick() {
        super.tick()
        body.rotation = Z_FORWARD
    }

    override fun draw() = Unit
}
