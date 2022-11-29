package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.Application
import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.util.y
import org.ode4j.math.DVector3C
import org.ode4j.ode.DBody
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.OdeHelper

abstract class Entity<T : Entity<T>>(
    val type: EntityType<T>,
    val level: Level,
    position: DVector3C
) {
    val body: DBody = OdeHelper.createBody(level.world)

    init {
        body.position = position
        @Suppress("LeakingThis")
        level.addEntity(this)
    }

    open fun collideWithMesh(collision: CollisionType, contact: DContactGeom, selfIsG1: Boolean): DSurfaceParameters? =
        when (collision) {
            CollisionTypes.SOLID,
            CollisionTypes.FLOOR -> Application.SURFACE_PARAMS
            CollisionTypes.WALL -> Application.WALL_PARAMS
            CollisionTypes.NON_SOLID -> null
            CollisionTypes.DEATH -> {
                kill()
                null
            }
            else -> Application.SURFACE_PARAMS
        }

    open fun collideWithEntity(other: Entity<*>, contact: DContactGeom, selfIsG1: Boolean): DSurfaceParameters? = null

    open fun kill() {
        level.removeEntity(this)
    }

    open fun preTick() = Unit

    open fun tick() {
        if (body.position.y <= -100) {
            kill()
        }
    }

    open fun destroy() {
        body.destroy()
    }

    abstract fun draw()
}
