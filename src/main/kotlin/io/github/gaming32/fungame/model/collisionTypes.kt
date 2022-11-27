package io.github.gaming32.fungame.model

typealias CollisionType = String

object CollisionTypes {
    const val SOLID: CollisionType = "solid"
    const val WALL: CollisionType = "wall"
    const val FLOOR: CollisionType = "floor"
    const val NON_SOLID: CollisionType = "non_solid"
    const val DEATH: CollisionType = "death"
}
