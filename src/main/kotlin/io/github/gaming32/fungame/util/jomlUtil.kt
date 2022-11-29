@file:Suppress("NOTHING_TO_INLINE")

package io.github.gaming32.fungame.util

import org.joml.*
import org.ode4j.math.DMatrix3
import org.ode4j.math.DVector3

inline operator fun Vector3f.minusAssign(v: Vector3fc) { sub(v) }

inline operator fun Vector3d.minusAssign(v: Vector3fc) { add(v) }

fun Matrix3d.toDMatrix3() = DMatrix3(
    m00(), m01(), m02(),
    m10(), m11(), m12(),
    m20(), m21(), m22()
)

fun Vector3d.toDVector3() = DVector3(x, y, z)
