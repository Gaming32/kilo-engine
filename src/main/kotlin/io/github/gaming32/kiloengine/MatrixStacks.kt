package io.github.gaming32.kiloengine

import org.joml.Matrix4fStack

class MatrixStacks(stackSize: Int) {
    val model: Matrix4fStack = Matrix4fStack(stackSize)
    val projection: Matrix4fStack = Matrix4fStack(stackSize)

    internal var uniModel = 0
    internal var uniProjection = 0
}
