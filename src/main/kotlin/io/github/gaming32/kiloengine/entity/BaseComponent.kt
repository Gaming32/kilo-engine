package io.github.gaming32.kiloengine.entity

import com.google.gson.JsonObject
import io.github.gaming32.kiloengine.KiloEngineGame
import io.github.gaming32.kiloengine.MatrixStacks
import io.github.gaming32.kiloengine.MouseMoveEvent
import io.github.gaming32.kiloengine.loader.SceneLoader
import io.github.gaming32.kiloengine.mesh.CollisionType
import io.github.gaming32.kiloengine.mesh.CollisionTypes
import io.github.gaming32.kiloengine.util.Destroyable
import io.github.gaming32.kiloengine.util.x
import io.github.gaming32.kiloengine.util.y
import io.github.gaming32.kiloengine.util.z
import org.joml.Vector3d
import org.ode4j.ode.DContact
import org.ode4j.ode.DContactGeom
import java.lang.invoke.MethodHandles
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class BaseComponent<T : BaseComponent<T>>(val type: ComponentType<T>, val entity: Entity) : Destroyable {
    companion object {
        internal val LOOKUP = MethodHandles.lookup()
    }

    abstract class ComponentType<T : BaseComponent<T>> {
        abstract fun create(entity: Entity, loader: SceneLoader, data: JsonObject): T
    }

    init {
        @Suppress("LeakingThis")
        entity.addComponent(this)
    }

    @OptIn(ExperimentalContracts::class)
    protected inline fun <T> drawPositioned(matrices: MatrixStacks, action: () -> T): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        matrices.model.pushMatrix()
        val position = entity.body.position
        matrices.model.translate(position.x.toFloat(), position.y.toFloat(), position.z.toFloat())
        val result = action()
        matrices.model.popMatrix()
        return result
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

    open fun draw(matrices: MatrixStacks) = Unit

    open fun handleMovement(movementInput: Vector3d) = Unit

    open fun drawUi(nanovg: Long) = Unit

    open fun mouseMoved(event: MouseMoveEvent) = Unit
}
