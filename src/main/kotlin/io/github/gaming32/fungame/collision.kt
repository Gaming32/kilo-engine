package io.github.gaming32.fungame

import io.github.gaming32.fungame.model.Model
import org.joml.Vector3d
import org.joml.Vector3f

fun collide(position: Vector3d, motion: Vector3d, level: Model) {
    for (tri in level.tris) {
        val normal = tri.a.normal!!
//        if (normal.y < 0.51f) continue
        if (isInTriangle(position, tri)) {
            position.y = tri.a.position.y.toDouble()
            motion.y = 0.0
        }
    }
}

private fun isInTriangle(pt: Vector3d, tri: Model.Tri): Boolean {
    if (tri.a.position.y - pt.y !in 0.0..0.75) {
        return false
    }

    if (pt.y > 1) {
        pt
    }
    val d1 = side(pt, tri.a.position, tri.b.position)
    val d2 = side(pt, tri.b.position, tri.c.position)
    val d3 = side(pt, tri.c.position, tri.a.position)

    val hasNeg = d1 < 0 || d2 < 0 || d3 < 0
    val hasPos = d1 > 0 || d2 > 0 || d3 > 0

    return !(hasNeg && hasPos)
}

private fun side(p1: Vector3d, p2: Vector3f, p3: Vector3f) =
    (p1.x - p3.x) * (p2.z - p3.z) - (p2.x - p3.x) * (p1.z - p3.z)
