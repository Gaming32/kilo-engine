@file:Suppress("NOTHING_TO_INLINE")

package io.github.gaming32.kiloengine.util

import com.google.gson.JsonArray
import org.joml.Matrix3f
import org.joml.Vector3f
import org.ode4j.math.DMatrix3C
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactBuffer
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.OdeConstants.dContactFDir1

inline val DVector3C.x get() = get0()
inline val DVector3C.y get() = get1()
inline val DVector3C.z get() = get2()

inline var DVector3.x
    get() = get0()
    set(x) = set0(x)

inline var DVector3.y
    get() = get1()
    set(y) = set1(y)

inline var DVector3.z
    get() = get2()
    set(z) = set2(z)

inline operator fun DVector3C.plus(other: DVector3C) = DVector3(x + other.x, y + other.y, z + other.z)

inline operator fun DVector3C.minus(other: DVector3C): DVector3 = reSub(other)

inline operator fun DVector3C.times(other: Double): DVector3 = reScale(other)

inline operator fun DVector3C.div(other: Double) = DVector3(x / other, y / other, z / other)

fun DContactGeom.copyFrom(other: DContactGeom) {
    pos.set(other.pos)
    normal.set(other.normal)
    depth = other.depth
    g1 = other.g1
    g2 = other.g2
    side1 = other.side1
    side2 = other.side2
}

fun DSurfaceParameters.copyFrom(other: DSurfaceParameters) {
    mode = other.mode
    mu = other.mu
    mu2 = other.mu2
    rho = other.rho
    rho2 = other.rho2
    rhoN = other.rhoN
    bounce = other.bounce
    bounce_vel = other.bounce_vel
    soft_erp = other.soft_erp
    soft_cfm = other.soft_cfm
    motion1 = other.motion1
    motion2 = other.motion2
    motionN = other.motionN
    slip1 = other.slip1
    slip2 = other.slip2
}

fun DContact(
    geom: DContactGeom? = null,
    surface: DSurfaceParameters? = null,
    fdir1: DVector3? = null
) = DContactBuffer(1).get(0)!!.also {
    if (geom != null) {
        it.geom.copyFrom(geom)
    }
    if (surface != null) {
        it.surface.copyFrom(surface)
    }
    if (fdir1 !== null) {
        if ((it.surface.mode and dContactFDir1) != 0) {
            throw IllegalArgumentException("Can only set fdir1 when dContactFDir1 is set.")
        }
        it.fdir1.set(fdir1)
    }
}

fun DVector3C.toVector3f() = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

fun JsonArray.toDVector3() = DVector3(this[0].asDouble, this[1].asDouble, this[2].asDouble)

fun DMatrix3C.toMatrix3f() = Matrix3f(
    get00().toFloat(), get01().toFloat(), get02().toFloat(),
    get10().toFloat(), get11().toFloat(), get12().toFloat(),
    get20().toFloat(), get21().toFloat(), get22().toFloat()
)
