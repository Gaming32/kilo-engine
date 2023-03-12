package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.KiloEngineGame
import io.github.gaming32.kiloengine.loader.LevelLoader
import io.github.gaming32.kiloengine.model.CollisionType
import io.github.gaming32.kiloengine.model.CollisionTypes
import io.github.gaming32.kiloengine.util.Destroyable
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
            CollisionTypes.FLOOR -> KiloEngineGame.SURFACE_PARAMS
            CollisionTypes.WALL -> KiloEngineGame.WALL_PARAMS
            CollisionTypes.NON_SOLID,
            CollisionTypes.DEATH -> null
            else -> KiloEngineGame.SURFACE_PARAMS
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
