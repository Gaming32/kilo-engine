package io.github.gaming32.fungame.parser

import io.github.gaming32.fungame.Level
import io.github.gaming32.fungame.entity.EntityRegistry
import io.github.gaming32.fungame.model.CollisionModel
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.Material
import io.github.gaming32.fungame.model.Model
import io.github.gaming32.fungame.util.simpleParentDir
import org.joml.Vector3f
import java.io.BufferedReader
import java.io.InputStream
import java.util.*

class LevelLoader(private val getResource: (String) -> InputStream?) {
    fun loadObj(name: String) = textResource(name) { inp, parentDir ->
        val verts = mutableListOf<Vector3f>()
        val normals = mutableListOf<Vector3f>()
        val uvs = mutableListOf<Model.UV>()
        val tris = mutableListOf<Model.Tri>()
        val materials = mutableMapOf<String, Material>()
        var material: Material? = null
        for (line in parseLines(inp)) {
            when (line[0]) {
                "mtllib" -> materials += loadMaterialLibrary(parentDir + line[1])
                "usemtl" -> material = materials[line[1]]
                "v" -> verts += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
                "vn" -> normals += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
                "vt" -> uvs += Model.UV(
                    line[1].toFloat(),
                    1f - line[2].toFloat()
                )
                "f" -> {
                    for (i in 2 until line.size - 1) {
                        tris += Model.Tri(
                            parseVertex(verts, normals, uvs, line[1]),
                            parseVertex(verts, normals, uvs, line[i]),
                            parseVertex(verts, normals, uvs, line[i + 1]),
                            material
                        )
                    }
                }
            }
        }
        Model(tris, materials)
    } ?: throw IllegalArgumentException("Missing OBJ model: $name")

    fun loadMaterialLibrary(name: String): Map<String, Material> = textResource(name) { inp, parentDir ->
        val materials = mutableMapOf<String, Material>()
        var currentMaterial = ""
        for (line in parseLines(inp)) {
            when (line[0]) {
                "newmtl" -> currentMaterial = line[1]
                "Kd" -> if (materials[currentMaterial] !is Material.Texture) {
                    materials[currentMaterial] = Material.Color(
                        line[1].toFloat(), line[2].toFloat(), line[3].toFloat(), line.getOrNull(4)?.toFloat() ?: 1f
                    )
                }
                "map_Kd" -> materials[currentMaterial] = Material.Texture(parentDir + line[1])
                "d" -> materials[currentMaterial].let { oldMat ->
                    val opacity = line[1].toFloat()
                    materials[currentMaterial] = when (oldMat) {
                        null -> Material.Color(1f, 1f, 1f, opacity)
                        is Material.Color -> oldMat.copy(a = opacity)
                        is Material.Texture -> oldMat.copy(color = oldMat.color.copy(a = opacity))
                    }
                }
            }
        }
        materials
    } ?: throw IllegalArgumentException("Missing material library: $name")

    fun loadCollision(model: Model, name: String) = textResource(name) { inp, _ ->
        val collisions = IdentityHashMap<Material, CollisionType>()
        for (line in parseLines(inp)) {
            when (line[0]) {
                "coltype" -> collisions[
                    model.materials[line[1]] ?: throw IllegalArgumentException("Missing material: ${line[1]}")
                ] = line[2]
            }
        }
        CollisionModel(model, collisions)
    } ?: throw IllegalArgumentException("Missing collision data: $name")

    fun loadLevel(name: String, level: Level = Level()) = textResource(name) { inp, parentDir ->
        var geom: Model? = null
        for (line in parseLines(inp)) {
            when (line[0]) {
                "geom" -> {
                    if (geom != null) {
                        throw IllegalArgumentException("geom can only be set once")
                    }
                    geom = loadObj(parentDir + line[1])
                }
                "collision" -> {
                    if (geom == null) {
                        throw IllegalArgumentException("geom must be set before collision")
                    }
                    level.setGeom(loadCollision(geom, parentDir + line[1]))
                }
                "entity" -> EntityRegistry.getType(line[1]).create(level, line.subList(2, 7))
            }
        }
        level
    } ?: throw IllegalArgumentException("Missing level: $name")

    private fun parseVertex(
        verts: List<Vector3f>,
        normals: List<Vector3f>,
        uvs: List<Model.UV>,
        vertex: String
    ): Model.Vertex {
        val parts = vertex.split("/")
        return Model.Vertex(
            verts[parts[0].toInt() - 1],
            if (parts.size > 2 && parts[2].isNotEmpty()) {
                normals[parts[2].toInt() - 1]
            } else {
                null
            },
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                uvs[parts[1].toInt() - 1]
            } else {
                null
            }
        )
    }

    private inline fun <T> textResource(
        name: String,
        action: (inp: BufferedReader, parentDir: String) -> T
    ) = getResource(name)?.bufferedReader(Charsets.UTF_8)?.use { action(it, simpleParentDir(name)) }

    private fun parseLines(inp: BufferedReader) = inp.lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .filter { !it.startsWith("#") }
        .map { it.split(" ") }
}
