package io.github.gaming32.kiloengine.mesh

import io.github.gaming32.kiloengine.util.FPI
import io.github.gaming32.kiloengine.util.ModelBuilder
import io.github.gaming32.kiloengine.util.plus
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

@JvmOverloads
fun ModelBuilder.inheritTri(v1: Vector3f, v2: Vector3f, v3: Vector3f, flipNormal: Boolean = false) {
    val current = elements.removeLast()
    elements += current.copy(position = if (flipNormal) v3 else v1)
    elements += current.copy(position = v2)
    elements += current.copy(position = if (flipNormal) v1 else v3)
    elements += current
}

fun ModelBuilder.quad(v1: Vector3f, v2: Vector3f, v3: Vector3f, v4: Vector3f) {
    inheritTri(v1, v2, v3)
    inheritTri(v1, v3, v4)
}

fun ModelBuilder.capsule(center: Vector3f, length: Float, radius: Float, sides: Int) {
    val halfLength = length / 2
    val bottomCenter = Vector3f(center).sub(0f, halfLength, 0f)
    val bottomEnd = Vector3f(bottomCenter).sub(0f, radius, 0f)
    val topCenter = Vector3f(center).add(0f, halfLength, 0f)
    val topEnd = Vector3f(topCenter).add(0f, radius, 0f)

    val radiansPerSide = 2 * FPI / sides
    val unit = Vector3f()
    val unit1 = Vector3f()
    repeat(sides) {
        val angle = it * radiansPerSide
        unit.x = cos(angle) * radius
        unit.z = sin(angle) * radius

        val angle1 = (it + 1) * radiansPerSide
        unit1.x = cos(angle1) * radius
        unit1.z = sin(angle1) * radius

        val bcu = bottomCenter + unit
        val bcu1 = bottomCenter + unit1
        val tcu = topCenter + unit
        val tcu1 = topCenter + unit1

        quad(bcu, bcu1, tcu1, tcu)
        inheritTri(tcu, tcu1, topEnd)
        inheritTri(bcu, bottomEnd, bcu1)
    }
}
