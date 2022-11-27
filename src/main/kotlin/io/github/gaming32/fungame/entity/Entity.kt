package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.World
import io.github.gaming32.fungame.model.CollisionModel
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.util.y
import org.ode4j.ode.DBody
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

abstract class Entity(val world: World, val geom: DGeom) {
    val body: DBody = OdeHelper.createBody(world.world)

    init {
        geom.body = body
        @Suppress("LeakingThis")
        world.addEntity(this)
    }

    open fun collideWith(model: CollisionModel, collision: CollisionType): Boolean = when (collision) {
        CollisionTypes.SOLID -> true
        CollisionTypes.NON_SOLID -> false
        CollisionTypes.DEATH -> {
            kill()
            false
        }
        else -> true
    }

    open fun collideWith(other: Entity) = Unit

    open fun kill() {
        world.removeEntity(this)
    }

    open fun tick() {
        if (body.position.y <= -100) {
            kill()
        }
    }
}
