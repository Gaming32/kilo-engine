@file:Suppress("NOTHING_TO_INLINE")
package io.github.gaming32.kiloengine.util

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

inline operator fun Vector2f.times(other: Float) = Vector2f(this).mul(other)!!

inline operator fun Vector2f.minus(other: Float) = Vector2f(this).sub(other, other)!!

inline fun Vector3f(xy: Vector2f, z: Float) = Vector3f(xy.x, xy.y, z)

inline fun Vector4f(xyz: Vector3f, w: Float) = Vector4f(xyz.x, xyz.y, xyz.z, w)

inline operator fun Vector4f.times(mat: Matrix4f) = Vector4f(this).mul(mat)!!

inline val Vector4f.xyz get() = Vector3f(x, y, z)

inline operator fun Matrix4f.times(vec: Vector4f) = Vector4f(vec).mul(this)!!
