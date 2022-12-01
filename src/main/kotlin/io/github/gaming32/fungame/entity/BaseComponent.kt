package io.github.gaming32.fungame.entity

import com.google.gson.JsonObject
import io.github.gaming32.fungame.Application
import io.github.gaming32.fungame.loader.LevelLoader
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.util.Destroyable
import org.ode4j.ode.DContact
import org.ode4j.ode.DContactGeom

abstract class BaseComponent<T : BaseComponent<T>>(val type: ComponentType<T>, val entity: Entity) : Destroyable {
    abstract class ComponentType<T : BaseComponent<T>> {
        abstract fun create(entity: Entity, loader: LevelLoader, data: JsonObject): T
    }

    init {
        @Suppress("LeakingThis")
        entity.addComponent(this)
    }

    open fun collideWithMesh(collision: CollisionType, contact: DContactGeom, selfIsG1: Boolean): DContact.DSurfaceParameters? =
        when (collision) {
            CollisionTypes.SOLID,
            CollisionTypes.FLOOR -> Application.SURFACE_PARAMS
            CollisionTypes.WALL -> Application.WALL_PARAMS
            CollisionTypes.NON_SOLID,
            CollisionTypes.DEATH -> null
            else -> Application.SURFACE_PARAMS
        }

    open fun collideWithEntity(
        other: Entity,
        contact: DContactGeom,
        selfIsG1: Boolean
    ): DContact.DSurfaceParameters? = null

    open fun preTick() = Unit

    open fun tick() = Unit

    open fun draw() = Unit
}
