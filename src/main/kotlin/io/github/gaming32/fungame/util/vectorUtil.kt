package io.github.gaming32.fungame.util

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector3fc

fun Vector3f(v: Vector3dc) = Vector3f(v.x().toFloat(), v.y().toFloat(), v.z().toFloat())

@Suppress("NOTHING_TO_INLINE")
inline operator fun Vector3f.minusAssign(v: Vector3fc) { sub(v) }

@Suppress("NOTHING_TO_INLINE")
inline operator fun Vector3d.minusAssign(v: Vector3fc) { add(v) }
