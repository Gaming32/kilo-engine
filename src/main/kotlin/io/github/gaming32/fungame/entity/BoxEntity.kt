package io.github.gaming32.fungame.entity

import com.google.gson.JsonObject
import io.github.gaming32.fungame.Application
import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.loader.LevelLoader
import io.github.gaming32.fungame.util.*
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN
import org.ode4j.math.DVector3
import org.ode4j.math.DVector3C
import org.ode4j.ode.DContactGeom
import org.ode4j.ode.OdeHelper

class BoxEntity(level: Level, position: DVector3C, size: DVector3C) : Entity<BoxEntity>(BoxEntity, level, position) {
    companion object Type : EntityType<BoxEntity>() {
        override fun create(level: Level, position: DVector3C, args: JsonObject, loader: LevelLoader) = BoxEntity(
            level, position, args["size"].asJsonArray.toDVector3()
        )
    }

    init {
        addGeom(OdeHelper.createBox(level.space, DVector3(size.x, size.y, size.z)))
    }

    private val jomlHalfSize = size.toVector3f().div(2f)

    override fun draw() = buildModel {
        disable(GL_TEXTURE_2D)
        color(0.5f, 0.5f, 1f, 1f)
        val jomlPos = body.position.toVector3f()

        // Bottom face
        begin(GL_TRIANGLE_FAN)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        draw()

        // Top face
        begin(GL_TRIANGLE_FAN)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        draw()

        // Left face
        begin(GL_TRIANGLE_FAN)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        draw()

        // Right face
        begin(GL_TRIANGLE_FAN)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        draw()

        // Front face
        begin(GL_TRIANGLE_FAN)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z - jomlHalfSize.z)
        draw()

        // Right face
        begin(GL_TRIANGLE_FAN)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y - jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x + jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        vertex(jomlPos.x - jomlHalfSize.x, jomlPos.y + jomlHalfSize.y, jomlPos.z + jomlHalfSize.z)
        draw()
    }

    override fun collideWithEntity(other: Entity<*>, contact: DContactGeom, selfIsG1: Boolean) =
        Application.SURFACE_PARAMS
}
