package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.loader.LevelLoader
import org.ode4j.math.DVector3C

object EntityRegistry {
    private val entityTypes = mutableMapOf<String, EntityType<*>>()

    fun register(id: String, type: EntityType<*>) {
        entityTypes[id] = type
    }

    fun getType(id: String) = entityTypes.getValue(id)

    init {
        register("box", BoxEntity)
        register("levelMesh", LevelMeshEntity)
        register("player", PlayerEntity)
    }
}

abstract class EntityType<T : Entity<T>> {
    abstract fun create(level: Level, position: DVector3C, args: List<String>, loader: LevelLoader): T
}
