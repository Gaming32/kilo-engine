package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.Application
import io.github.gaming32.fungame.World
import io.github.gaming32.fungame.model.CollisionModel
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.ode4j.math.DVector3
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.OdeHelper

class PlayerEntity(world: World) : Entity(
    world,
    OdeHelper.createCapsule(world.space, 0.4, 1.0).also { geom ->
        geom.rotation = Application.Z_FORWARD
    }
) {
    companion object {
        val START_POS = DVector3(0.0, 0.0, -5.0)
        val UP = DVector3(0.0, 1.0, 0.0)
    }

    var lastJumpCollidedTime = 0.0
    val jumpNormal = DVector3(UP)

    init {
        body.position = START_POS
    }

    override fun kill() {
        body.position = START_POS
        body.linearVel = DVector3.ZERO
    }

    override fun collideWith(model: CollisionModel, collision: CollisionType, contact: DContactGeom): Boolean {
        val result = super.collideWith(model, collision, contact)
        if (collision == CollisionTypes.WALL || collision == CollisionTypes.FLOOR) {
            lastJumpCollidedTime = glfwGetTime()
            jumpNormal.set(
                if (collision == CollisionTypes.FLOOR) {
                    UP
                } else {
                    contact.normal
                }
            )
        }
        return result
    }
}
