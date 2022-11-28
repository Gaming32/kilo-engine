package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.util.y
import org.ode4j.math.DVector3C
import org.ode4j.ode.DBody
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

abstract class Entity<T : Entity<T>>(
    val type: EntityType<T>,
    val level: Level,
    val geom: DGeom,
    position: DVector3C
) {
    val body: DBody = OdeHelper.createBody(level.world)

    init {
        geom.body = body
        body.position = position
        @Suppress("LeakingThis")
        level.addEntity(this)
    }

    open fun collideWithLevel(collision: CollisionType, contact: DContactGeom, levelFirst: Boolean): Boolean =
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
    open fun collideWithEntity(other: Entity<*>, contact: DContactGeom) = false

    open fun kill() {
        level.removeEntity(this)
    }

    open fun preTick() = Unit

    open fun tick() {
        if (body.position.y <= -100) {
            kill()
        }
    }

    abstract fun draw()
}
