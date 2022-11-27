package io.github.gaming32.fungame

import io.github.gaming32.fungame.entity.PlayerEntity
import io.github.gaming32.fungame.model.CollisionType
import io.github.gaming32.fungame.model.CollisionTypes
import io.github.gaming32.fungame.obj.ObjLoader
import io.github.gaming32.fungame.util.*
import org.joml.*
import org.joml.Math.*
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL2.nvgCreate
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_MULTISAMPLE
import org.lwjgl.opengl.GLUtil
import org.ode4j.math.DVector3
import org.ode4j.ode.DContact.DSurfaceParameters
import org.ode4j.ode.DContactGeomBuffer
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeConstants.*
import org.ode4j.ode.OdeHelper
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.roundToLong

class Application {
    companion object {
        private const val MOUSE_SPEED = 0.5
        private const val MOVE_SPEED = 85.0
        private const val JUMP_SPEED = 500.0
        private const val WALL_JUMP_HORIZONTAL = 1500.0
        private const val WALL_JUMP_VERTICAL = 500.0
        private const val PHYSICS_SPEED = 0.02
        private const val CONTACT_COUNT = 16
        private const val VERTICAL_DAMPING = 0.01
        private const val HORIZONTAL_DAMPING = 0.17
        val Z_FORWARD = Matrix3d().rotateX(PI / 2).toDMatrix3()
        private val SURFACE_PARAMS = DSurfaceParameters().apply {
            mu = 0.0
        }
        private val WALL_PARAMS = DSurfaceParameters().apply {
            mu = 3.0
        }
        private val DEC_FORMAT = DecimalFormat("0.0")
    }

    init {
        OdeHelper.initODE()
    }

    private val world = World()
    private val windowSize = Vector2i()
    private val player = PlayerEntity(world)
    private val movementInput = Vector3d()
    private val rotation = Vector2f()
    private var wireframe = false
    private var window = 0L
    private var nanovg = 0L
    private var clearParams = GL_DEPTH_BUFFER_BIT
    private lateinit var objLoader: ObjLoader

    fun main() {
        init()
        registerEvents()
        val skybox = withValue(-1, TextureManager::maxMipmap, { TextureManager.maxMipmap = it }) {
            withValue(GL_NEAREST, TextureManager::filter, { TextureManager.filter = it }) {
                objLoader.loadObj("/skybox/skybox.obj").toDisplayList()
            }
        }
        val level = objLoader.loadObj("/example/example.obj")
        val levelBody = OdeHelper.createBody(world.world)
        val levelCollision = objLoader.parseCollision(level, "/example/example_collision.txt")
        val collisionMeshes = mutableMapOf<DGeom, CollisionType>()
        for ((collision, mesh) in levelCollision.toMultiTriMeshData()) {
            val geom = OdeHelper.createTriMesh(
                world.space, mesh,
                { _, _, _ -> 1 },
                { _, _, _, _ -> },
                { _, _, _, _, _ -> 1 }
            )
            collisionMeshes[geom] = collision
            geom.body = levelBody
        }
        levelBody.setKinematic()
        val levelList = level.toDisplayList()
        var lastTime = glfwGetTime()
        var lastPhysicsTime = lastTime
        var fpsAverage = 0.0
        val contactJointGroup = OdeHelper.createJointGroup()
        val force = DVector3()
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()
            glClear(clearParams)

            val time = glfwGetTime()
            val deltaTime = time - lastTime
            val fps = 1 / deltaTime
            fpsAverage = 0.95 * fpsAverage + 0.05 * fps
            lastTime = time

            if (time - lastPhysicsTime > 1.0) {
                lastPhysicsTime = time - 1.0
            }
            while (time - lastPhysicsTime > PHYSICS_SPEED) {
                world.forEachEntity { it.preTick() }
                val adjustedMovementInput = Vector3d(movementInput).rotateY(
                    toRadians(rotation.y.toDouble())
                )
                player.body.addForce(adjustedMovementInput.x * MOVE_SPEED, 0.0, adjustedMovementInput.z * MOVE_SPEED)
                if (movementInput.y > 0.0 && glfwGetTime() - player.lastJumpCollidedTime <= 0.1) {
                    if (player.jumpNormal.y < 0.95) {
                        player.body.addForce(
                            0.0,
                            movementInput.y * (WALL_JUMP_VERTICAL - player.body.linearVel.y),
                            0.0
                        )
                        player.body.addForce(DVector3(player.jumpNormal).scale(movementInput.y * WALL_JUMP_HORIZONTAL))
                    } else {
                        player.body.addForce(0.0, movementInput.y * JUMP_SPEED, 0.0)
                    }
                }
                movementInput.y = 0.0
                world.forEachEntity { entity ->
                    entity.body.addForce(
                        entity.body.linearVel.x * -HORIZONTAL_DAMPING / PHYSICS_SPEED,
                        entity.body.linearVel.y * -VERTICAL_DAMPING / PHYSICS_SPEED,
                        entity.body.linearVel.z * -HORIZONTAL_DAMPING / PHYSICS_SPEED
                    )
                }
                player.body.rotation = Z_FORWARD
                force.set(player.body.force)
                val contactBuffer = DContactGeomBuffer(CONTACT_COUNT)
                contactJointGroup.clear()
                world.space.collide(null) { _, o1, o2 ->
                    repeat(OdeHelper.collide(o1, o2, CONTACT_COUNT, contactBuffer)) {
                        val contact = contactBuffer[it]
                        val surfaceParams = if (contact.g1.body == levelBody) {
                            if (collisionMeshes.getValue(contact.g1) == CollisionTypes.WALL) {
                                WALL_PARAMS
                            } else {
                                SURFACE_PARAMS
                            }
                        } else if (contact.g2.body == levelBody) {
                            if (collisionMeshes.getValue(contact.g2) == CollisionTypes.WALL) {
                                WALL_PARAMS
                            } else {
                                SURFACE_PARAMS
                            }
                        } else {
                            return@repeat // Just don't collide for now
                        }
                        val joint = OdeHelper.createContactJoint(
                            world.world,
                            contactJointGroup,
                            DContact(contact, surfaceParams)
                        )
                        joint.attach(contact.g1.body, contact.g2.body)
                        if (contact.g1.body == levelBody) {
                            world.getEntityByGeom(contact.g2)
                                .collideWith(levelCollision, collisionMeshes.getValue(contact.g1), contact)
                        } else if (contact.g2.body == levelBody) {
                            world.getEntityByGeom(contact.g1)
                                .collideWith(levelCollision, collisionMeshes.getValue(contact.g2), contact)
                        } else {
                            val e1 = world.getEntityByGeom(contact.g1)
                            val e2 = world.getEntityByGeom(contact.g2)
                            e1.collideWith(e2, contact)
                            e2.collideWith(e1, contact)
                        }
                    }
                }
                world.world.quickStep(PHYSICS_SPEED)
                world.forEachEntity { it.tick() }
                lastPhysicsTime += PHYSICS_SPEED
            }

            // 3D Mode
            glEnable(GL_CULL_FACE)
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            gluPerspective(80f, windowSize.x.toFloat() / windowSize.y, 0.01f, 1000f)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
            glRotatef(rotation.x, 1f, 0f, 0f)
            if (player.jumpNormal.y < 0.95 && glfwGetTime() - player.lastJumpCollidedTime < 0.1) {
                val horizAngle = normalizeDegrees(
                    toDegrees(atan2(player.jumpNormal.z, player.jumpNormal.x)) - 90 -
                        rotation.y
                )
                if (horizAngle < 0) {
                    glRotatef(5f, 0f, 0f, 1f)
                } else if (horizAngle > 0) {
                    glRotatef(-5f, 0f, 0f, 1f)
                }
            }
            glRotatef(180 - rotation.y, 0f, 1f, 0f)

            // Skybox
            skybox.draw()

            // Level
            glEnable(GL_DEPTH_TEST)
            glEnable(GL_LIGHTING)
            val position = player.body.position
            glTranslatef(-position.x.toFloat(), -position.y.toFloat() - 0.7f, -position.z.toFloat())
            glLightfv(
                GL_LIGHT0, GL_POSITION, floatArrayOf(
                    position.x.toFloat() - 12.9f,
                    position.y.toFloat() + 30f,
                    position.z.toFloat() + 17.1f,
                    0f
                )
            )

            levelList.draw(/* ModelBuilder(), collisions */)

            // HUD
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, windowSize.x.toDouble(), windowSize.y.toDouble(), 0.0, 1.0, 3.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
            glTranslatef(0f, 0f, -2f)
            glDisable(GL_DEPTH_TEST)
            glDisable(GL_LIGHTING)

            val widthArray = IntArray(1)
            glfwGetFramebufferSize(window, widthArray, null)
            nvgBeginFrame(
                nanovg,
                windowSize.x.toFloat(), windowSize.y.toFloat(),
                widthArray[0].toFloat() / windowSize.x
            )
            nvgFontFace(nanovg, "minecraftia")
            nvgText(nanovg, 10f, 35f, "FPS: ${fpsAverage.roundToLong()}")
            nvgText(
                nanovg, 10f, 55f,
                "X/Y/Z: " +
                    "${DEC_FORMAT.format(position.x)}/" +
                    "${DEC_FORMAT.format(position.y)}/" +
                    DEC_FORMAT.format(position.z)
            )
            nvgText(
                nanovg, 10f, 75f,
                "FX/FY/FZ: " +
                    "${DEC_FORMAT.format(force.x)}/" +
                    "${DEC_FORMAT.format(force.y)}/" +
                    DEC_FORMAT.format(force.z)
            )
            nvgText(
                nanovg, 10f, 95f,
                "VX/VY/VZ: " +
                    "${DEC_FORMAT.format(player.body.linearVel.x)}/" +
                    "${DEC_FORMAT.format(player.body.linearVel.y)}/" +
                    DEC_FORMAT.format(player.body.linearVel.z)
            )
            nvgText(
                nanovg, 10f, 115f,
                "RY/RX: " +
                    "${DEC_FORMAT.format(rotation.y)}/" +
                    DEC_FORMAT.format(rotation.x)
            )
            nvgEndFrame(nanovg)

            glfwSwapBuffers(window)
        }
        skybox.close()
        levelList.close()
        contactJointGroup.destroy()
        quit()
    }

    private fun init() {
        GLFWErrorCallback.createPrint().set()
        if (!glfwInit()) {
            throw Exception("Failed to initialize GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_SAMPLES, 4)
//        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)

//        val monitor = glfwGetPrimaryMonitor()
//        val videoMode = glfwGetVideoMode(monitor) ?: throw Exception("Could not determine video mode")

        windowSize.set(1280, 720)
        window = glfwCreateWindow(windowSize.x, windowSize.y, "Fun 3D Game", 0, 0)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()
        GLUtil.setupDebugMessageCallback()

        nanovg = nvgCreate(NVG_ANTIALIAS)
        loadFont(nanovg, "minecraftia")

        glfwSwapInterval(1)

        glfwSetWindowSizeCallback(window) { _, width, height ->
            windowSize.set(width, height)
            glViewport(0, 0, windowSize.x, windowSize.y)
        }
        glViewport(0, 0, windowSize.x, windowSize.y)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_MULTISAMPLE)

        glClearColor(0f, 0f, 0f, 1f)
        glShadeModel(GL_SMOOTH)
        glEnable(GL_LIGHT0)
        glMaterialfv(GL_FRONT, GL_AMBIENT, floatArrayOf(1f, 1f, 1f, 1f))
        glLineWidth(10f)

        objLoader = ObjLoader(TextureManager.getResource)
    }

    private fun registerEvents() {
        val lastMousePos = Vector2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        glfwSetCursorPosCallback(window) { _, x, y ->
            if (
                lastMousePos.x != Double.POSITIVE_INFINITY &&
                glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
            ) {
                rotation.y = normalizeDegrees(rotation.y - ((x - lastMousePos.x) * MOUSE_SPEED).toFloat())
                rotation.x = clamp(-90f, 90f, rotation.x + ((y - lastMousePos.y) * MOUSE_SPEED).toFloat())
            }
            lastMousePos.set(x, y)
        }

        glfwSetMouseButtonCallback(window) { _, _, action, _ ->
            if (action == GLFW_PRESS) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            }
        }

        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            if (action == GLFW_REPEAT) return@glfwSetKeyCallback
            val press = action == GLFW_PRESS
            if (glfwGetInputMode(window, GLFW_CURSOR) != GLFW_CURSOR_DISABLED) {
                return@glfwSetKeyCallback
            }
            when (key) {
                GLFW_KEY_ESCAPE -> if (press) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                    movementInput.x = 0.0
                    movementInput.z = 0.0
                }
                GLFW_KEY_W -> if (press) {
                    movementInput.z++
                } else {
                    movementInput.z--
                }
                GLFW_KEY_S -> if (press) {
                    movementInput.z--
                } else {
                    movementInput.z++
                }
                GLFW_KEY_A -> if (press) {
                    movementInput.x++
                } else {
                    movementInput.x--
                }
                GLFW_KEY_D -> if (press) {
                    movementInput.x--
                } else {
                    movementInput.x++
                }
                GLFW_KEY_SPACE -> if (press) {
                    movementInput.y = 1.0
                }
                GLFW_KEY_F3 -> if (press) {
                    wireframe = !wireframe
                    @Suppress("LiftReturnOrAssignment")
                    if (wireframe) {
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                        glLineWidth(2f)
                        clearParams = GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT
                    } else {
                        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
                        glLineWidth(10f)
                        clearParams = GL_DEPTH_BUFFER_BIT
                    }
                }
            }
        }
    }

    private fun quit() {
        world.destroy()
        OdeHelper.closeODE()
        TextureManager.unload()
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}

fun main() = Application().main()
