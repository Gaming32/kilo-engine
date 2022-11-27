@file:Suppress("NOTHING_TO_INLINE")

package io.github.gaming32.fungame.util

import org.joml.*
import org.ode4j.math.DMatrix3
import org.ode4j.math.DVector3C

inline fun Vector3f(v: Vector3dc) = Vector3f(v.x().toFloat(), v.y().toFloat(), v.z().toFloat())

inline fun Vector3d(v: DVector3C) = Vector3d(v.x, v.y, v.z)

inline operator fun Vector3f.minusAssign(v: Vector3fc) { sub(v) }

inline operator fun Vector3d.minusAssign(v: Vector3fc) { add(v) }

fun Matrix3d.toDMatrix3() = DMatrix3(
    m00(), m01(), m02(),
    m10(), m11(), m12(),
    m20(), m21(), m22()
)
