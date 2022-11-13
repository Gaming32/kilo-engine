package io.github.gaming32.fungame

import io.github.gaming32.fungame.model.Model
import io.github.gaming32.fungame.util.FPI
import io.github.gaming32.fungame.util.Vector3f
import io.github.gaming32.fungame.util.minusAssign
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun collide(position: Vector3d, motion: Vector3d, level: Model, collisions: MutableList<Model.Tri>) {
    for (tri in level.tris) {
        if (tri.a.normal!!.y < 0.1f) continue
        if (isInTriangle(position, tri)) {
            collisions += tri
            motion.y = 0.0
        }
    }
}

private fun isInTriangle(pt: Vector3d, tri: Model.Tri): Boolean {
    val origin = tri.a.position
    val normal = tri.a.normal!!
    val a = Vector3f()
    val b = normalize(Vector3f(tri.b.position), origin, normal)
    val c = normalize(Vector3f(tri.c.position), origin, normal)
    val pt2 = normalize(Vector3f(pt), origin, normal)

    if (-pt2.y !in 0.0..0.75) {
        return false
    }

    val d1 = side(pt2, a, b)
    val d2 = side(pt2, b, c)
    val d3 = side(pt2, c, a)

    val hasNeg = d1 < 0 || d2 < 0 || d3 < 0
    val hasPos = d1 > 0 || d2 > 0 || d3 > 0

    if (!hasNeg || !hasPos) {
        pt.x -= pt2.y * normal.x
        pt.y -= pt2.y * normal.y
        pt.z -= pt2.y * normal.z
        return true
    }
    return false
}

/** Mutates [pt]. */
private fun normalize(pt: Vector3f, origin: Vector3f, normal: Vector3f): Vector3f {
    pt -= origin
//    pt.x *= 0.5f / normal.x
//    pt.y = 0f
//    pt.z *= 0.5f / normal.z
    val xAngle = FPI / 2f - atan2(normal.y, normal.x) // Radians
    val xSin = sin(xAngle)
    val xCos = cos(xAngle)
    pt.x = pt.x * xCos - pt.y * xSin
    pt.y = pt.x * xSin + pt.y * xCos

    val zAngle = FPI / 2f - atan2(normal.y, normal.z) // Radians
    val zSin = sin(zAngle)
    val zCos = cos(zAngle)
    pt.z = pt.z * zCos - pt.y * zSin
    pt.y = pt.z * zSin + pt.y * zCos
    return pt
}

private fun side(p1: Vector3f, p2: Vector3f, p3: Vector3f) =
    (p1.x - p3.x) * (p2.z - p3.z) - (p2.x - p3.x) * (p1.z - p3.z)
