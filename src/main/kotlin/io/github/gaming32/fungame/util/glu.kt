package io.github.gaming32.fungame.util

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glMultMatrixf
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI as PI_DOUBLE

private const val PI = PI_DOUBLE.toFloat()

private val IDENTITY_MATRIX = floatArrayOf(
    1.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 1.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 1.0f
)

private val matrix: FloatBuffer = BufferUtils.createFloatBuffer(16)

private fun gluMakeIdentityf(m: FloatBuffer) {
    val oldPos = m.position()
    m.put(IDENTITY_MATRIX)
    m.position(oldPos)
}

fun gluPerspective(fovy: Float, aspect: Float, zNear: Float, zFar: Float) {
    val sine: Float
    val cotangent: Float
    val radians = fovy / 2 * PI / 180
    val deltaZ = zFar - zNear
    sine = sin(radians.toDouble()).toFloat()
    if (deltaZ == 0f || sine == 0f || aspect == 0f) {
        return
    }
    cotangent = cos(radians.toDouble()).toFloat() / sine
    gluMakeIdentityf(matrix)
    matrix.put(0 * 4 + 0, cotangent / aspect)
    matrix.put(1 * 4 + 1, cotangent)
    matrix.put(2 * 4 + 2, -(zFar + zNear) / deltaZ)
    matrix.put(2 * 4 + 3, -1f)
    matrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ)
    matrix.put(3 * 4 + 3, 0f)
    glMultMatrixf(matrix)
}
