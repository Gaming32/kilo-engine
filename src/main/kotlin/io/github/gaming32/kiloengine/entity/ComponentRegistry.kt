package io.github.gaming32.kiloengine.entity

import io.github.gaming32.kiloengine.entity.BaseComponent.ComponentType

object ComponentRegistry {
    private val componentTypes = mutableMapOf<String, ComponentType<*>>()

    fun register(id: String, type: ComponentType<*>) {
        componentTypes[id] = type
    }

    @Deprecated("Renamed", replaceWith = ReplaceWith("ComponentRegistry.get(id)"))
    fun getType(id: String) = componentTypes.getValue(id)

    @JvmStatic
    operator fun get(identifier: String) = componentTypes.getValue(identifier)

    init {
        register("camera", CameraComponent)
        register("capsuleCollider", CapsuleColliderComponent)
        register("mesh", MeshComponent)
        register("meshCollider", MeshColliderComponent)
        register("meshRenderer", MeshRendererComponent)
        register("player", PlayerComponent)
    }
}
