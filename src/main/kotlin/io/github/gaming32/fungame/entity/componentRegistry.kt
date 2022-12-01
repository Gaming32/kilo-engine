package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.entity.BaseComponent.ComponentType

object ComponentRegistry {
    private val componentTypes = mutableMapOf<String, ComponentType<*>>()

    fun register(id: String, type: ComponentType<*>) {
        componentTypes[id] = type
    }

    fun getType(id: String) = componentTypes.getValue(id)

    init {
        register("camera", CameraComponent)
        register("capsuleCollider", CapsuleColliderComponent)
        register("mesh", MeshComponent)
        register("meshCollider", MeshColliderComponent)
        register("meshRenderer", MeshRendererComponent)
        register("player", PlayerComponent)
    }
}
