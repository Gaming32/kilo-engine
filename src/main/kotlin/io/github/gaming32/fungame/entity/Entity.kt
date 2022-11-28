package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.World
import io.github.gaming32.fungame.model.CollisionModel
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.util.y
import org.ode4j.ode.DBody
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

abstract class Entity(val world: World, val geom: DGeom) {
    val body: DBody = OdeHelper.createBody(world.world)

    init {
        geom.body = body
        @Suppress("LeakingThis")
        world.addEntity(this)
    }

    open fun collideWith(model: CollisionModel, collision: CollisionType, contact: DContactGeom): Boolean =
        when (collision) {
            CollisionTypes.SOLID,
            CollisionTypes.WALL,
            CollisionTypes.FLOOR -> true
            CollisionTypes.NON_SOLID -> false
            CollisionTypes.DEATH -> {
                kill()
                false
            }
            else -> true
        }

    /**
     * @return `true` if this entity should collide solidly with the other entity.
     */
    open fun collideWith(other: Entity, contact: DContactGeom) = false

    open fun kill() {
        world.removeEntity(this)
    }

    open fun preTick() = Unit

    open fun tick() {
        if (body.position.y <= -100) {
            kill()
        }
    }
}
