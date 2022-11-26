@file:Suppress("NOTHING_TO_INLINE")

package io.github.gaming32.fungame.util

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.ode4j.math.DVector3C

inline fun Vector3f(v: Vector3dc) = Vector3f(v.x().toFloat(), v.y().toFloat(), v.z().toFloat())

inline fun Vector3d(v: DVector3C) = Vector3d(v.x, v.y, v.z)

inline operator fun Vector3f.minusAssign(v: Vector3fc) { sub(v) }

inline operator fun Vector3d.minusAssign(v: Vector3fc) { add(v) }
