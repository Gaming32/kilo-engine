package io.github.gaming32.kiloengine.loader

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.gaming32.gson5.Gson5Reader
import io.github.gaming32.kiloengine.Level
import io.github.gaming32.kiloengine.entity.ComponentRegistry
import io.github.gaming32.kiloengine.entity.Entity
import io.github.gaming32.kiloengine.model.CollisionModel
import io.github.gaming32.kiloengine.model.CollisionType
import io.github.gaming32.kiloengine.model.Material
import io.github.gaming32.kiloengine.model.Model
import io.github.gaming32.kiloengine.util.ResourceGetter
import io.github.gaming32.kiloengine.util.simpleParentDir
import io.github.gaming32.kiloengine.util.toDVector3
import org.joml.Vector3f
import org.ode4j.math.DVector3
import org.quiltmc.json5.JsonReader
import java.io.BufferedReader
import java.util.*

class LevelLoaderImpl(private val resourceGetter: () -> ResourceGetter) : LevelLoader {
    override fun loadObj(name: String) = textResource(name) { inp, parentDir ->
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

    override fun loadMaterialLibrary(name: String) = textResource(name) { inp, parentDir ->
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
                "map_Kd" -> materials[currentMaterial] = Material.Texture(
                    if (line[1].startsWith("~")) {
                        line[1]
                    } else {
                        parentDir + line[1]
                    }
                )
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

    override fun loadCollision(model: Model, name: String) = textResource(name) { inp, _ ->
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

    override fun loadLevel(name: String, level: Level) = textResource(name) { inp, _ ->
        val json = JsonParser.parseReader(Gson5Reader(JsonReader.json5(inp))).asJsonObject
        json["entities"]?.asJsonArray?.forEach { entityData ->
            entityData as JsonObject
            val position = entityData.remove("position")?.asJsonArray?.toDVector3() ?: DVector3()
            val kinematic = entityData.remove("kinematic")?.asBoolean ?: false
            val componentsData = entityData.remove("components")?.asJsonArray?.asList() ?: listOf()
            val entity = Entity(level, position)
            if (kinematic) {
                entity.body.setKinematic()
            }
            for (componentData in componentsData) {
                if (componentData.isJsonPrimitive) {
                    ComponentRegistry.getType(componentData.asString)
                        .create(entity, this, JsonObject())
                } else {
                    componentData as JsonObject
                    ComponentRegistry.getType(componentData.remove("type").asString)
                        .create(entity, this, componentData)
                }
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
    ) = resourceGetter()(name)?.bufferedReader(Charsets.UTF_8)?.use { action(it, simpleParentDir(name)) }

    private fun parseLines(inp: BufferedReader) = inp.lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .filter { !it.startsWith("#") }
        .map { it.split(" ") }
}
